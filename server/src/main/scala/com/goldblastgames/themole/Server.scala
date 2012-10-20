package com.goldblastgames.themole

import scalaz._
import Scalaz._

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Switcher
import com.github.oetzi.echo.io.Receiver
import com.github.oetzi.echo.io.Sender
import com.github.oetzi.echo.io.Stdin

import com.goldblastgames.themole.chat.ChatEffect
import com.goldblastgames.themole.gambits.AppliedEffect
import com.goldblastgames.themole.io.DeadDrop
import com.goldblastgames.themole.io.GambitCommand
import com.goldblastgames.themole.io.Message
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.SubmitCommand
import com.goldblastgames.themole.server.ServerModule
import com.goldblastgames.themole.server.Session
import com.goldblastgames.themole.mission.TimedMissionChange
import com.goldblastgames.themole.Nation.America
import com.goldblastgames.themole.Nation.USSR

object Server extends EchoApp {

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
      Player("william", America, America),
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

      // Gambit system
      val gambitModule = ServerModule.gambit()
      val gambitInput = connections.mapValues(_.filter(_.isInstanceOf[GambitCommand]).map((_, cmd) => cmd.asInstanceOf[GambitCommand]))
      val (gambitMessages, gambitEffects): (Map[Player, Event[Message]], Behaviour[Seq[AppliedEffect[Message, Message]]]) = gambitModule(gambitInput)
      val chatEffects: Behaviour[Seq[AppliedEffect[Message,Message]]] = gambitEffects.map(effects => effects) // once we have more effects, select the right one.

      // Chat system
      val chatModule = ServerModule.chat(chatEffects)
      val chatInput = connections
          .mapValues(_.filter(_.isInstanceOf[Message]).map((_, msg) => msg.asInstanceOf[Message]))
      val chatOutput = chatModule(chatInput)
          .mapValues(_.map((_, msg) => msg.asInstanceOf[Packet]))

      // Mission system
      val missionModule = ServerModule.mission(new TimedMissionChange(14400000L * 6))
      val missionInput = connections
          .mapValues(_.filter(_.isInstanceOf[SubmitCommand]).map((_, msg) => msg.asInstanceOf[SubmitCommand]))
      val missionOutput = missionModule(missionInput)
          .mapValues(_.map((_, msg) => msg.asInstanceOf[Packet]))

      // Dead-drop system
      val deadDropModule = ServerModule.deadDrop(14400000L)
      val deadDropInput = connections
          .mapValues(_.filter(_.isInstanceOf[DeadDrop]).map((_, msg) => msg.asInstanceOf[DeadDrop]))
      val deadDropOutput = deadDropModule(deadDropInput)
          .mapValues(_.map((_, msg) => msg.asInstanceOf[Packet]))

      // Allow server to send messages directly to players
      val dmModule: ServerModule[Packet, Message] = ServerModule({ sources =>
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
    )
      val dmOutput = dmModule(Map())
          .mapValues(_.map((_, msg) => msg.asInstanceOf[Packet]))

      // Join all the resulting streams and output them.
      join(chatOutput, missionOutput, deadDropOutput, dmOutput)
    }

    println("Server started!")
  }

  // Merge different streams.
  // This is done weirdly, see: http://stackoverflow.com/questions/7755214/
  def join(maps: Map[Player, Event[Packet]]*) = maps
      .map(_.mapValues(Seq(_)))
      .reduce(_ |+| _)
      .mapValues(_.reduce(_ merge _))
}
