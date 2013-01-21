package com.goldblastgames.themole.server

import java.lang.Thread

import com.github.oetzi.echo.core.EventSource
import unfiltered.netty.websockets._

import java.io.InputStream
import java.io.OutputStream

/**
 * Listens for WebSocket connections and
 * is an event of WebSocket, PlayerName tuples
**/
case class Listener(
  val port: Int
) extends EventSource[(WebSocket, String)] {

  // TODO: Only send the WebSocket/String combo
  // on receiving a Connect message

  val server = WebSocketServer("/", port) {
    case Open(s) => {
      println("Socket opened!") // TODO: remove this, just for debugging
    }
    case Message(s, Text(str)) => {
      println("Received message: %s from socket %s".format(str, s))
      occur(s, str)
    }
    case Close(s) => // TODO: what to do when we close sockets?
    // TODO: log errors
    case Error(s,e) => println("error %s".format(e.getMessage))
  }

  server.start()
}

// TODO: move these somewhere else
class WSOutputStream(s: WebSocket) extends OutputStream {
  override def write(b: Array[Byte]) {
    // TODO: check this is correct
    s.send(b.toString)
  }

  override def write(i: Int) {
    s.send(i.toString)
  }
}

