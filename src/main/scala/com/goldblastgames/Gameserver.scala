package com.goldblastgames.server

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour

import com.goldblastgames.Player
import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.Message
import com.goldblastgames.Nation._

object GameServer extends EchoApp {

  def setup(args: Array[String]) {
    // TODO(Issue-16): This should be in configuration somewhere.
    val port = 2552
    val players = Seq(
      Player("daisy", America, America),
      Player("robert", America, America),
      Player("franklin", America, USSR),
      Player("kane", USSR, USSR),
      Player("aaron", USSR, USSR),
      Player("clayton", USSR, America)
    )
    val effects = Seq[ChatEffect](
      ChatEffect.redact(Behaviour(_ => true), _ => true),
      ChatEffect.shuffle(Behaviour(_ => true), _ => true)
    )

    print("Starting server on port %d...".format(port))

    val session = new Session(port, players)

    // Build the socket sources.
    val receivers = session.connections.values

    // Chat system
    val chatModule = ServerModule.chat(effects)
    val messageSources = receivers
        .map(_.filter(_ matches Message.messageRegex).map((_, msg) => Message.deserialize(msg)))
    val chatStreams = chatModule(players.zip(messageSources).toMap)

    // Merge different streams.
    val senderStreams = chatStreams

    val senders = senderStreams.foreach {
      case (player, stream) => session.connections(player).write(stream.map((_, msg) => msg.toString))
    }

    println("Server started!")
  }
}
