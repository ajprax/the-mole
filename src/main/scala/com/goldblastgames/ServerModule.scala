package com.goldblastgames

import com.github.oetzi.echo.core.Event

import com.goldblastgames.Nation._

case class ServerModule[T] (
  frp: Map[Player, Event[T]] => Map[Player, Event[T]]
) extends (Map[Player, Event[T]] => Map[Player, Event[T]]) {
  def apply(sources: Map[Player, Event[T]]) = frp(sources)
}

object ServerModule {
  def chat(effects: Seq[ChatEffect]): ServerModule[Message] = {

    def channel(name: String, input: Event[Message]) = input.filter(_.channel == name)

    ServerModule(sources => {
      // Merge sources.
      val allSources     = sources.values.reduce(_ merge _)
      val ussrSources    = sources.filterKeys(_.camp == USSR).values.reduce(_ merge _)
      val americaSources = sources.filterKeys(_.camp == America).values.reduce(_ merge _)

      // Create channels.
      val allChannel     = channel("All", allSources)
      val playerChannels = Map(sources.keys.map(player => (player -> channel(player.name, allSources))).toSeq: _*)
      val campChannels   = Map(
        USSR -> channel("USSR", ussrSources),
        America -> channel("America", americaSources)
      )

      // Build sets of player's sink components.
      val sinks = sources.keys.map {
        case p @ Player(_, sinkPort, _, name, nation, _) => {
          val appliedEffects = effects.filter(_.select(p))
          val channelSinks = allChannel.merge(campChannels(nation)).merge(playerChannels(p))

          // TODO: Use these timestamps.
          p -> channelSinks.map((_, msg) => appliedEffects.foldLeft(msg)((x, f) => f(x)))
        }
      }
      Map(sinks.toSeq: _*)
    })
  }
}
