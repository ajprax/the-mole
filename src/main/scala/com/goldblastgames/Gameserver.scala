package com.goldblastgames

import scalaz._
import Scalaz._

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.io.Receiver
import com.github.oetzi.echo.io.Sender

import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.server.ServerModule
import com.goldblastgames.server.Session
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

    val messageSources = receivers
      .map(_.filter(_ matches Message.messageRegex).map((_, msg) => Message.deserialize(msg)))
    val playerMessageSources = Map((players zip messageSources): _*)

    val commandSources = receivers
          .map(_.filter(_ matches SubmitCommand.submitRegex)
            .map((_, msg) => SubmitCommand.deserialize(msg)))
    val playerCommandSources = Map((players zip commandSources): _*)

    // Chat system
    val chatModule = ServerModule.chat(effects)
    val chatSources = chatModule(playerMessageSources)

    // Mission system
    val missionModule = ServerModule.mission()
    val missionSources = missionModule(playerCommandSources)
    
    // Merge different streams.
    // This is done weirdly, see: http://stackoverflow.com/questions/7755214/
   val senderStreams: Map[Player, Event[Message]] = 
     (missionSources.mapValues(Seq(_)) |+| chatSources.mapValues(Seq(_)) ).map{case (k, v) => k -> v.reduce(_ merge _)}
    
    val senders = senderStreams.foreach {
      case (player, stream) => session.connections(player).write(stream.map((_, msg) => msg.toString))
    }

    println("Server started!")
  }
}
