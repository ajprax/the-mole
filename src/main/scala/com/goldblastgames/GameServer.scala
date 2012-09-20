package com.goldblastgames

import scalaz._
import Scalaz._

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.io.Receiver
import com.github.oetzi.echo.io.Sender

import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.DeadDrop
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.server.ServerModule
import com.goldblastgames.server.Session
import com.goldblastgames.mission.TimedMissionChange
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

      // Print raw input.
      connections.foreach { case (player, connection) =>
        connection.foreach(msg => println("Raw from %s: %s".format(player, msg)))
      }

      // Chat system
      val chatModule = ServerModule.chat(effects)
      val chatInput = connections
          .mapValues(_.filter(_ matches Message.messageRegex).map((_, msg) => Message.deserialize(msg)))
      val chatOutput = chatModule(chatInput)
          .mapValues(_.map((_, msg) => msg.toString))

      // Mission system
      val missionModule = ServerModule.mission(new TimedMissionChange(10000))
      val missionInput = connections
          .mapValues(_.filter(_ matches SubmitCommand.submitRegex).map((_, msg) => SubmitCommand.deserialize(msg)))
      val missionOutput = missionModule(missionInput)
          .mapValues(_.map((_, msg) => msg.toString))

      // Dead-drop system
      /* val deadDropModule = ServerModule.deadDrop(14400000L) */
      val deadDropModule = ServerModule.deadDrop(5000L)
      val deadDropInput = connections
          .mapValues(_.filter(_ matches DeadDrop.deadDropRegex).map((_, msg) => DeadDrop.deserialize(msg)))
      val deadDropOutput = deadDropModule(deadDropInput)
          .mapValues(_.map((_, msg) => msg.toString))


      // Join all the resulting streams and output them.
      join(chatOutput, missionOutput, deadDropOutput)
    }

    println("Server started!")
  }

  // Merge different streams.
  // This is done weirdly, see: http://stackoverflow.com/questions/7755214/
  def join(maps: Map[Player, Event[String]]*) = maps
      .map(_.mapValues(Seq(_)))
      .reduce(_ |+| _)
      .mapValues(_.reduce(_ merge _))
}
