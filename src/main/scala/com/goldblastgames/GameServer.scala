package com.goldblastgames

import scalaz._
import Scalaz._

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Switcher
import com.github.oetzi.echo.io.Receiver
import com.github.oetzi.echo.io.Sender
import com.github.oetzi.echo.io.Stdin

import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.DeadDrop
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.server.ServerModule
import com.goldblastgames.server.Session
import com.goldblastgames.mission.TimedMissionChange
import com.goldblastgames.Nation._

object GameServer extends EchoApp {

  val effectRegex = """/effect (\w+) (.+) (\d*\.?\d+)"""
  val effectMatcher = effectRegex.r

  def effectEnable(enables: Event[(String, Behaviour[Boolean])], effect: String): Behaviour[Boolean] = {
    val filtered = enables
        .filter({ case (name, _) => effect == name })
        .map((_, x) => x._2)

    Switcher(Behaviour(_ => false), filtered)
  }

  val statusRegex = """/status *"""
  val statusMatcher = statusRegex.r

  val dmRegex = """/say (\S+) (.*)"""
  val dmMatcher = dmRegex.r

  def setup(args: Array[String]) {
    // Setup server console.
    // Handle effect enables
    val effectEnables = Stdin.filter(_ matches effectRegex)
        .map { (begin, command) =>
          val effectMatcher(effect, player, duration) = command
          val end = begin + duration.toDouble

          println("Enabling %s effect on player %s for %f".format(effect, player, duration.toDouble))

          (effect, Behaviour(t => { println("Checking %s's enable status. %f < %f < %f".format(effect, begin, t, end)); begin < t && t < end } ))
        }
    val redactEnable = effectEnable(effectEnables, "redact")
    val shuffleEnable = effectEnable(effectEnables, "shuffle")
    val anonymizeEnable = effectEnable(effectEnables, "anonymize")

    // Handle status queries.
    val statusEvent = Stdin.filter(_ matches statusRegex)
    redactEnable
        .sample(statusEvent)
        .foreach { case (_, enabled) => println("Redaction: %s".format(if (enabled) "on" else "off")) }
    shuffleEnable
        .sample(statusEvent)
        .foreach { case (_, enabled) => println("Shuffle: %s".format(if (enabled) "on" else "off")) }
    anonymizeEnable
        .sample(statusEvent)
        .foreach { case (_, enabled) => println("Anonymize: %s".format(if (enabled) "on" else "off")) }

    // TODO(Issue-16): This should be in configuration somewhere.
    val port = 2552
    val players = Seq(
      Player("aaron", America, America),
      Player("christophe", America, America),
      Player("kane", America, USSR),
      Player("clayton", USSR, USSR),
      Player("daisy", USSR, USSR),
      Player("franklin", USSR, America)
    )
    val effects = Seq[ChatEffect](
      ChatEffect.redact(redactEnable, _ => true),
      ChatEffect.shuffle(shuffleEnable, _ => true),
      ChatEffect.anonymize(anonymizeEnable, _ => true)
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
      val missionModule = ServerModule.mission(new TimedMissionChange(14400000L))
      val missionInput = connections
          .mapValues(_.filter(_ matches SubmitCommand.submitRegex).map((_, msg) => SubmitCommand.deserialize(msg)))
      val missionOutput = missionModule(missionInput)
          .mapValues(_.map((_, msg) => msg.toString))

      // Dead-drop system
      val deadDropModule = ServerModule.deadDrop(14400000L)
      val deadDropInput = connections
          .mapValues(_.filter(_ matches DeadDrop.deadDropRegex).map((_, msg) => DeadDrop.deserialize(msg)))
      val deadDropOutput = deadDropModule(deadDropInput)
          .mapValues(_.map((_, msg) => msg.toString))

      // Allow server to send messages directly to players
      val dmModule = ServerModule[String, Message] { sources =>
        val playerNameMap = players.map(_.name).zip(players).toMap

        val dmMessages = Stdin.filter(_ matches dmRegex)
            .map { (_, command) =>
              val dmMatcher(player, message) = command

              (player, message)
            }

        players
            .map(player => (player, dmMessages.filter { case (name, _) => player.name == name }))
            .toMap
            .map { case (player, messages) => (player, messages.map((_, msg) => { println("Sending server message: %s".format(msg._2)); Message("server", player.name, msg._2) } )) }
      }
      val dmOutput = dmModule(Map())
          .mapValues(_.map((_, msg) => msg.toString))

      // Join all the resulting streams and output them.
      join(chatOutput, missionOutput, deadDropOutput, dmOutput)
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
