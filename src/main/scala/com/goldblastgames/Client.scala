package com.goldblastgames

import scala.io.Source

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.EventSource
import com.github.oetzi.echo.core.Stepper
import com.github.oetzi.echo.io.Sender
import com.github.oetzi.echo.io.Stdin
import com.github.oetzi.echo.io.Receiver

object Client extends EchoApp {
  def setup(args: Array[String]) {
    val logger = LoggerFactory.getLogger(this.getClass)

    require(
      args.length == 3, 
      "Specify name, sourceport, and sinkport as arguments"
    )
    val name = args(0)
    val sourcePort = args(1).toInt
    val sinkPort = args(2).toInt
    logger.info(
      "Started client with name %s on sourcePort %s and sinkPort %s"
        .format(name, sourcePort, sinkPort)
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
    val source = Receiver(sourcePort)(msg => Behaviour(_ => msg))
    val sink = Sender("localhost", sinkPort, messages)

    // Print incoming messages.
    source.foreach { received: String =>
      val msg = Message.deserialize(received)
      println("[ %s -> %s ]: %s".format(msg.sender, msg.channel, msg.body))
    }
  }
}
