package com.goldblastgames.server

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import com.github.oetzi.echo.io.Server

import com.goldblastgames.Player
import com.goldblastgames.io.Connect
import com.goldblastgames.io.Packet

class Session private(
  val port: Int,
  val players: Seq[Player],
  val module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]
) {

  type SocketStreams = (ObjectInputStream, ObjectOutputStream)

  val server = new EventSource[(String, SocketStreams)] {
    Server(port, x => x)
        .foreach { socket =>
          val out: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
          val in: ObjectInputStream = new ObjectInputStream(socket.getInputStream)

          println("Waiting for connection string...")
          in.readObject match {
            case Connect(name) => {
              println("Player %s connected.".format(name))
              occur((name, (in, out)))
            }

            case x => {
              println("Invalid connection packet: %s".format(x.toString))
              sys.error("Received a bad connection packet: %s".format(x.toString))
            }
          }
        }
  }

  private def playerFilter(input: Event[(String, SocketStreams)], name: String) = input
      .filter({ case (n, _) => n == name })
      .map((_, x) => x._2)

  val inputs: Map[Player, PlayerSocket] = players
      .map(p => p -> new PlayerSocket(playerFilter(server, p.name), p.name))
      .toMap

  val outputs: Map[Player, PlayerConnection] = module(inputs)
      .map { case (p, output) => p -> new PlayerConnection(inputs(p), output) }
}

object Session {
  def apply(port: Int, players: Seq[Player])(module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]) =
      new Session(port, players, module)
}
