package com.goldblastgames

import _root_.reactive.EventStream

import com.goldblastgames.Nation._

case class ServerModule[T] (
  frp: Map[Player, EventStream[T]] => Map[Player, EventStream[T]]
) extends (Map[Player, EventStream[T]] => Map[Player, EventStream[T]]) {
  def apply(sources: Map[Player, EventStream[T]]) = frp(sources)
}

object ServerModule {
  def chat(effects: Seq[ChatEffect]): ServerModule[Message] = {

    def channel(name: String, input: EventStream[Message]) = input.filter(_.channel == name)

    ServerModule(sources => {
      // Merge sources.
      val allSources     = sources.values.reduce(_ | _)
      val ussrSources    = sources.filterKeys(_.camp == USSR).values.reduce(_ | _)
      val americaSources = sources.filterKeys(_.camp == America).values.reduce(_ | _)
    
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
          val sinks = allChannel | campChannels(nation) | playerChannels(p)

          p -> sinks.map(msg => appliedEffects.foldLeft(msg)((x, f) => f(x)))
        }
      }
      Map(sinks.toSeq: _*)
    })
  }
}
