package com.goldblastgames.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

class PlayerConnection(
  val connect: Event[Socket],
  val name: String
) extends EventSource[String] {

  // TODO: Make this not crash when the socket gets closed.
  var out: Option[PrintWriter] = None

  connect.foreach { socket =>
    // Update output.
    PlayerConnection.this.synchronized {
      out = Some(new PrintWriter(socket.getOutputStream, true))
      println("Updating %s's socket".format(name))
    }

    // Add new input.
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val thread = new Thread(new Runnable() {
      def run() {
        Iterator.continually(in.readLine).foreach(occur(_))
      }
    })
    thread.start()
  }

  // TODO: Buffer messages instead of dropping them.
  def write(data: Event[String]) {
    data.foreach(msg => synchronized { out.map(_.println(msg)) })
  }
}
