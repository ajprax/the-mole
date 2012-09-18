package com.goldblastgames.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.io.Server

import com.goldblastgames.Player
import com.goldblastgames.io.Connect

class Session private(
  val port: Int,
  val players: Seq[Player],
  val module: Map[Player, Event[String]] => Map[Player, Event[String]]
) {

  private def connect(socket: Socket): (String, Socket) = {
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val Connect(name) = Connect.deserialize(in.readLine)
    println("Player %s connected.".format(name))

    (name, socket)
  }

  val server = Server(port, connect)

  private def playerFilter(input: Event[(String, Socket)], name: String) = input
    .filter({ case (n, _) => n == name })
    .map((_, x) => x._2)

  val inputs: Map[Player, PlayerSocket] = players
      .map(p => p -> new PlayerSocket(playerFilter(server, p.name), p.name))
      .toMap

  val outputs: Map[Player, PlayerConnection] = module(inputs)
      .map { case (p, output) => p -> new PlayerConnection(inputs(p), output) }
}

object Session {
  def apply(port: Int, players: Seq[Player])(module: Map[Player, Event[String]] => Map[Player, Event[String]]) =
      new Session(port, players, module)
}
