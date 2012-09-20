package com.goldblastgames.client

import scala.io.Source

import java.io.PrintWriter
import java.net.Socket

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Stepper
import com.github.oetzi.echo.io.Connection
import com.github.oetzi.echo.io.Stdin

import com.goldblastgames.io.Connect
import com.goldblastgames.io.DeadDrop
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.skills.Skills

object PlayerSender {
  def apply(name: String, host: String, port: Int, messages: Event[String]): Connection = {
    val socket = new Socket(host, port)
    val out = new PrintWriter(socket.getOutputStream, true)
    out.println(Connect(name).toString)
    Connection(socket, messages)
  }
}

object Client extends EchoApp {
  val logger = LoggerFactory.getLogger(this.getClass)

  val channelRegex = """/channel (.+)"""
  val channelMatcher = channelRegex.r

  val deadDropRegex = """/deaddrop (.+)"""
  val deadDropMatcher = deadDropRegex.r

  val submitRegex = """/submit (\w+) (\w+)"""
  val submitMatcher = submitRegex.r

  def setup(args: Array[String]) {

    // Make sure the right arguments have been specified.
    require(
      args.length == 2, 
      "Specify name and port as arguments"
    )

    // Extract the arguments.
    val name = args(0)
    val port = args(1).toInt
    logger.info(
      "Started client with name %s on port %s"
        .format(name, port)
    )



    // Handle channel switches.
    val channelSwitch = Stdin.filter(_ matches channelRegex).map { (_, msg) =>
      val channelMatcher(channel) = msg
      channel
    }
    val dest = Stepper("All", channelSwitch)

    // TODO submit commands
    // Handle skill submissions.
    val commands: Event[String] = Stdin.filter(in => in matches submitRegex).map {
      (_, msg) => {
        val List(skill, amt) = submitRegex.r.unapplySeq(msg).get
        new SubmitCommand(name, Skills.withName(skill), amt.toInt)
      }
    }.map((_, msg) => msg.toString)

    // Handle deaddrops.
    val deadDrops = Stdin.filter(_ matches deadDropRegex).map { (_, msg) =>
      val deadDropMatcher(deadDrop) = msg
      DeadDrop(deadDrop).toString
    }

    // Handle everything else.
    val messages = Stdin.filter(in => !(in matches channelRegex) && !(in matches deadDropRegex) && !(in matches submitRegex))
        // Build a new Message.
        .map((_, msg) => new Message(name, dest.eval, msg))
        // Perform serialization.
        .map((_, msg) => msg.toString)

    val sender = PlayerSender(name, "localhost", port, commands merge messages merge deadDrops)



    // Print incoming messages.
    sender.foreach { received: String =>
      println("Raw: %s".format(received))

      received match {

        case DeadDrop.deadDropMatcher(drop) =>
            println("[ DEADDROP ]: %s".format(drop))

        case Message.messageMatcher(sender, channel, body) =>
            println("[ %s -> %s ]: %s".format(sender, channel, body))
      }
    }
  }
}
