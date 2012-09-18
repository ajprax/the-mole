package com.goldblastgames.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import com.github.oetzi.echo.core.Stepper

class PlayerSocket(
  val connect: Event[Socket],
  val name: String
) extends EventSource[String] {

  connect.foreach { socket =>

    val thread = new Thread(new Runnable() {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

      def run() {
        Iterator.continually(in.readLine).takeWhile(_ != null).foreach(occur(_))
        println("Closing %s's socket".format(name))
        in.close()
        socket.close()
      }
    })
    thread.start()
  }

  val history = this.foldLeft(Seq[String]())(_ ++ Seq(_))
}

class PlayerConnection(
  val socket: PlayerSocket,
  val output: Event[String]
) extends EventSource[String] {

  val printWriters = socket.connect.map((_, s) => new PrintWriter(s.getOutputStream, true))

  // Send messages.
  val writers = printWriters.foldLeft(Seq[PrintWriter]())((seq, writer) => seq ++ Seq(writer))
  writers.sample(output)
      .foreach { case (msg, writer) => writer.foreach(_.println(msg)) }

  // Forward messages from input.
  socket.foreach(occur(_))

  // Store message history.
  val history = output.foldLeft(Seq[String]())(_ ++ Seq(_))

  history.sample(printWriters)
      .foreach { case (writer, hist) => hist.foreach(msg => { writer.println(msg) } ) }
}
