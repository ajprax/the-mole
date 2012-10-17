package com.goldblastgames.themole.server

import java.lang.Thread
import java.net.ServerSocket
import java.net.Socket

import com.github.oetzi.echo.core.EventSource

case class Listener(
  val port: Int
) extends EventSource[Socket] {

  private val thread: Thread = new Thread(new Runnable() {
    def run() {
      val socket: ServerSocket = new ServerSocket(port)

      // Accept connections.
      val connections: Iterator[Socket] = Iterator.continually(socket.accept())
      connections.foreach(occur(_))

      // Clean up once done.
      connections.foreach(_.close())
      socket.close()
    }
  })
  thread.start()
}
