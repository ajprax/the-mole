package com.goldblastgames.themole.server

import java.lang.Thread

import com.github.oetzi.echo.core.EventSource
import unfiltered.netty.websockets._

case class Listener(
  val port: Int
) extends EventSource[WebSocket] {

  private val thread: Thread = new Thread(new Runnable() {
    def run() {
      WebSocketServer("/", port) {
        case Open(s) => occur(s)
        case Message(s, Text(str)) => println("message %s".format(str))
        case Close(s) => println("should close %s".format(s))
        // TODO: log errors
        case Error(s,e) => println("error %s".format(e.getMessage))
      }
    }
  })
  thread.start()
}
