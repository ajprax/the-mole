package com.goldblastgames

// TODO: Remove the reactive package.
import _root_.reactive.EventStream
import _root_.reactive.EventSource
import _root_.reactive.Observing
import _root_.reactive.Val

import com.goldblastgames.reactive.socket.SocketSource
import com.goldblastgames.reactive.socket.SocketSink

import com.goldblastgames.Nation._

object Gameserver extends App {
  def channel(name: String, input: EventStream[Message]) = input.filter(_.channel == name)

  // TODO: This should be in configuration somewhere.
  val players = Seq(
    Player(2552, 2562, 1L, "Player1", America, America),
    Player(2553, 2563, 2L, "Player2", USSR, USSR),
    Player(2554, 2564, 3L, "Player3", America, USSR),
    Player(2555, 2565, 4L, "Player4", USSR, America)
  )
  // TODO: This should be loaded when abilities have been selected.
  val effects = Seq[ChatEffect](
    // TODO: Move these to a different file.
    ChatEffect.redact(Val(true), _ => true)

  )

  val playerNames = players.map(_.name)

  // Build the socket sources.
  val socketSources = players
      .map(_.sourcePort)
      .map(SocketSource[Any]("127.0.1.1", _))
  val messageSources = socketSources
      .map(_.filter {
          case message: Message => true
          case _ => false
        }
      )
      // TODO: Fix this badness. No typecast should be necessary here
      .map(_.asInstanceOf[EventStream[Message]])
  val allSources = messageSources.reduce(_ | _)
  val sources = Map((players zip messageSources): _*)

  // Get source ports.
  val sinkPorts = players.map(_.sinkPort)

  // Create channels.
  val allChannel = channel("All", allSources)
  val playerChannels = Map(playerNames.map(name => (name, channel(name, allSources))): _*)

  // TODO: Combine these
  val ussr    = players
      .filter(_.camp == USSR)
      .map(sources(_))
      .reduce(_ | _)
      .filter(_.channel == "USSR")
  val america = players
      .filter(_.camp == America)
      .map(sources(_))
      .reduce(_ | _)
      .filter(_.channel == "America")
  val campChannels = Map(
    USSR -> ussr,
    America -> america
  )

  // TODO: Add logging
  // TODO: Add ChatEffects
  // Build sets of player's sink components.
  val sinks = players.map {
    case p @ Player(_, sinkPort, _, name, nation, _) => {
      val appliedEffects = effects.filter(_.select(p))
      val all = allChannel
          .map(msg => Message(msg.sender, "All", msg.body))
          .map(msg => appliedEffects.foldLeft(msg)((x, f) => f(x)))
      val camp = campChannels(nation)
          .map(msg => Message(msg.sender, nation.toString, msg.body))
          .map(msg => appliedEffects.foldLeft(msg)((x, f) => f(x)))
      val player = playerChannels(name)
          .map(msg => Message(msg.sender, name, msg.body))
          .map(msg => appliedEffects.foldLeft(msg)((x, f) => f(x)))
      SocketSink[Message]("127.0.1.1", sinkPort, all | camp | player)
    }
  }
}
