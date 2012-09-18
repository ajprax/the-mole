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

object PlayerSender {
  def apply(name: String, host: String, port: Int, messages: Event[String]): Connection = {
    val socket = new Socket(host, port)
    val out = new PrintWriter(socket.getOutputStream, true)
    out.println(Connect(name).toString)
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

    val messages = Stdin.filter(in => !(in matches channelRegex))
        // Build a new Message.
        .map((_, msg) => new Message(name, dest.eval, msg))
        // Perform serialization.
        .map((_, msg) => msg.toString)
    val sender = PlayerSender(name, "localhost", port, messages)

    // Print incoming messages.
    sender.foreach { received: String =>
      println("Raw: %s".format(received))
      val msg = Message.deserialize(received)
      println("[ %s -> %s ]: %s".format(msg.sender, msg.channel, msg.body))
    }
  }
}
