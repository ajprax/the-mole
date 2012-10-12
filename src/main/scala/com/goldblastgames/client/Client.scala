package com.goldblastgames.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Stepper
import com.github.oetzi.echo.io.Stdin

import com.goldblastgames.io.DeadDrop
import com.goldblastgames.io.Message
import com.goldblastgames.io.Packet
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.skills.Skills

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
    val commands: Event[Packet] = Stdin
        .filter(in => in matches submitRegex)
        // Build a new Command.
        .map { (_, msg) =>
          val submitMatcher(skill, amt) = msg
          SubmitCommand(name, Skills.withName(skill), amt.toInt)
        }

    // Handle deaddrops.
    val deadDrops: Event[Packet] = Stdin
        .filter(_ matches deadDropRegex)
        // Build a new DeadDrop.
        .map { (_, msg) =>
          val deadDropMatcher(deadDrop) = msg
          DeadDrop(deadDrop)
        }

    // Handle everything else.
    val messages: Event[Packet] = Stdin
        .filter(in => !(in matches channelRegex) && !(in matches deadDropRegex) && !(in matches submitRegex))
        // Build a new Message.
        .map { (_, msg) => 
          Message(name, dest.eval, msg)
        }

    val output: Event[Packet] = commands merge messages merge deadDrops
    output.foreach(packet => logger.debug("Sending packet: %s".format(packet)))

    val (in, out) = ClientConnection(name, "localhost", port, output)


    // Print incoming messages.
    in.foreach { received: Packet =>
      logger.debug("Received packet: %s".format(received))

      received match {
        case DeadDrop(drop) => println("[ DEADDROP ]: %s".format(drop))
        case Message(sender, channel, body) => println("[ %s -> %s ]: %s".format(sender, channel, body))
      }
    }
  }
}
