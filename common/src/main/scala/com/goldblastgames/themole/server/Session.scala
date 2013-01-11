package com.goldblastgames.themole.server

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.DataInputStream
import java.io.DataOutputStream

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

import com.goldblastgames.themole.Player
import com.goldblastgames.themole.io.Connect
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._

/**
 * Provides the input/output capabilities for a server session.
 *
 * module is the actual behaviour of the server
**/

class Session private(
  val port: Int,
  val players: Seq[Player],
  val module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]
) {

  // The connections from players and corresponding inputs/outputs
  val server = new EventSource[(String, (WSEvent, DataOutputStream))] {
    Listener(port)
        .foreach { case (socket, input) =>
          val out: DataOutputStream = new DataOutputStream(new WSOutputStream(socket))
          // val in: DataInputStream = new DataInputStream(input)

          println("Waiting for connection string...")
          // val data = in.readUTF
          val allData = input.foldLeft(Seq[String]())((acc, p) => acc ++ Seq[String](p))
          val data = {
            while(allData.eval.size < 1) {
              Thread.sleep(1000)
            }
            val curr = allData.eval
            curr(0)
          }

          deserialize(data) match {
            case Connect(name) => {
              println("Player %s connected.".format(name))
              occur((name, (input, out)))
            }

            case x => {
              println("Invalid connection packet: %s".format(x.toString))
              sys.error("Received a bad connection packet: %s".format(x.toString))
            }
          }
        }
  }

  // Only inputs from players
  val inputStreams: Event[(String, WSEvent)] = server.map { (_, connection) =>
    val (name, (inputStream, _)) = connection

    (name, inputStream)
  }

  // Only outputs to players
  val outputStreams: Event[(String, DataOutputStream)] = server.map { (_, connection) =>
    val (name, (_, outputStream)) = connection

    (name, outputStream)
  }

  val inputs: Map[Player, PlayerInput] = players
      .map({ player =>
        val input = inputStreams
            .filter({ case (name, _) => name == player.name })
            .map((_, x) => x._2)

        (player, new PlayerInput(player.name, input))
      })
      .toMap

  val moduleOutputs = module(inputs)

  val outputs: Map[Player, PlayerOutput] = players
      .map({ player =>
        val output = outputStreams
            .filter({ case (name, _) => name == player.name })
            .map((_, x) => x._2)

        (player, new PlayerOutput(player.name, output, moduleOutputs(player)))
      })
      .toMap
}

object Session {
  def apply(port: Int, players: Seq[Player])(module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]) =
      new Session(port, players, module)
}
