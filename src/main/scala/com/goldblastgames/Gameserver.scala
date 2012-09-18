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

    val session = Session(port, players) { connections =>

      // Chat system
      val chatModule = ServerModule.chat(effects)
      val chatInput = connections.values
          .map(_.filter(_ matches Message.messageRegex).map((_, msg) => Message.deserialize(msg)))
      val chatOutput = chatModule(players.zip(chatInput).toMap)
          .mapValues(_.map((_, msg) => msg.toString))

      // Mission system
      val missionModule = ServerModule.mission()
      val missionInput = connections.values
          .map(_.filter(_ matches SubmitCommand.submitRegex).map((_, msg) => SubmitCommand.deserialize(msg)))
      val missionOutput = missionModule(players.zip(missionInput).toMap)
          .mapValues(_.map((_, msg) => msg.toString))

      // Merge different streams.
      // This is done weirdly, see: http://stackoverflow.com/questions/7755214/
      val output: Map[Player, Event[String]] = 
          (missionOutput.mapValues(Seq(_)) |+| chatOutput.mapValues(Seq(_)))
          .map { case (k, v) => k -> v.reduce(_ merge _) }
      output
    }

    println("Server started!")
  }
}
