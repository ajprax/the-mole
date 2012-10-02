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
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.skills.Skills

object PlayerSender {
  def apply(name: String, host: String, port: Int, messages: Event[String]): Connection = {
    val socket = new Socket(host, port)
    val out = new PrintWriter(socket.getOutputStream, true)
    out.println(Connect(name).toString)
    // messages.foreach(x => println(x))
    Connection(socket, messages)
  }
}

object Client extends EchoApp {
  def setup(args: Array[String]) {
    val logger = LoggerFactory.getLogger(this.getClass)

    require(
      args.length == 2, 
      "Specify name and port as arguments"
    )
    val name = args(0)
    val port = args(1).toInt
    logger.info(
      "Started client with name %s on port %s"
        .format(name, port)
    )

    val channelRegex = """/channel (.+)"""
    val channelMatcher = channelRegex.r

    // Handle channel switches.
    val channelSwitch = Stdin.filter(_ matches channelRegex).map { (_, msg) =>
      val channelMatcher(channel) = msg
      channel
    }
    val dest = Stepper("All", channelSwitch)

    // TODO submit commands
    val submitRegex = """/submit (\w+) (\w+)"""
    val commands: Event[String] = Stdin.filter(in => in matches submitRegex).map {
      (_, msg) => {
        val List(skill, amt) = submitRegex.r.unapplySeq(msg).get
        new SubmitCommand(name, Skills.withName(skill), amt.toInt)
      }
    }.map((_, msg) => msg.toString)

    val messages: Event[String] = Stdin.filter(in => !(in matches channelRegex) && !(in matches submitRegex))
        // Build a new Message.
        .map((_, msg) => new Message(name, dest.eval, msg))
        // Perform serialization.
        .map((_, msg) => msg.toString)
    val sender = PlayerSender(name, "localhost", port, commands merge messages)

    // Print incoming messages.
    sender.foreach { received: String =>
      println("Raw: %s".format(received))
      val msg = Message.deserialize(received)
      println("[ %s -> %s ]: %s".format(msg.sender, msg.channel, msg.body))
    }
  }
}
