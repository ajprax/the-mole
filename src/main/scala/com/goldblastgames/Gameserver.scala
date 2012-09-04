package com.goldblastgames

// TODO(Issue-14): Remove the reactive package.
import _root_.reactive.Val

import com.goldblastgames.reactive.socket.SocketSource
import com.goldblastgames.reactive.socket.SocketSink

import com.goldblastgames.Nation._

object Gameserver extends App {
  // TODO(Issue-16): This should be in configuration somewhere.
  val players = Seq(
    Player(2552, 2562, 1L, "Player1", America, America),
    Player(2553, 2563, 2L, "Player2", USSR, USSR),
    Player(2554, 2564, 3L, "Player3", America, USSR),
    Player(2555, 2565, 4L, "Player4", USSR, America)
  )
  val effects = Seq[ChatEffect](
    ChatEffect.redact(Val(true), _ => true),
    ChatEffect.shuffle(Val(true), _ => true)
  )

  // Build the socket sources.
  val socketSources = players
      .map(_.sourcePort)
      .map(SocketSource[Any]("127.0.1.1", _))

  // Chat system
  val chatModule = ServerModule.chat(effects)
  val messageSources = socketSources
      .map(_.collect { case message: Message => message })
  val chatStreams = chatModule(Map((players zip messageSources): _*))

  // Merge different streams.
  val sinkStreams = chatStreams
  
  val sinks = sinkStreams.map {
    case (player, stream) => SocketSink[Any]("127.0.1.1", player.sinkPort, stream)
  }
}
