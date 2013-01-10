package com.goldblastgames.themole.server

import java.lang.Thread

import com.github.oetzi.echo.core.EventSource
import unfiltered.netty.websockets._

import java.io.InputStream
import java.io.OutputStream

case class Listener(
  val port: Int
) extends EventSource[(WebSocket, EventSource[String])] {

  private val thread: Thread = new Thread(new Runnable() {
    def run() {
      val inputs = Map[WebSocket, WSEvent]()
      WebSocketServer("/", port) {
        case Open(s) => {
          val in = new WSEvent()
          occur((s, in))
        }
        case Message(s, Text(str)) => {
          // TODO: send messages through the inputstream
          inputs(s).messageOccur(str)
        }
        case Close(s) => // TODO: what to do when we close sockets?
        // TODO: log errors
        case Error(s,e) => println("error %s".format(e.getMessage))
      }
    }
  })
  thread.start()
}

// TODO: move these somewhere else
class WSOutputStream(s: WebSocket) extends OutputStream {
  def write(b: Array[Byte]) {
    // TODO: check this is correct
    s.send(b.toString)
  }
}

class WSEvent extends EventSource[String] {
  // ??
  def messageOccur(s: String) {
    occur(s)
  }
}
