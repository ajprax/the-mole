package com.goldblastgames.server

import java.io.EOFException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import com.github.oetzi.echo.core.Stepper

import com.goldblastgames.io.Packet

class PlayerSocket(
  val connect: Event[(ObjectInputStream, ObjectOutputStream)],
  val name: String
) extends EventSource[Packet] {

  connect.foreach { case (in, _) =>

    val thread = new Thread(new Runnable() {
      def run() {
        Iterator
            .continually({
              try {
                in.readObject
              } catch {
                case ex: EOFException => null
              }
            })
            .takeWhile(_ != null)
            .collect({ case x: Packet => x })
            .foreach(occur(_))
        println("Closing %s's socket".format(name))
        in.close()
      }
    })
    thread.start()
  }

  val history = this.foldLeft(Seq[Packet]())(_ ++ Seq(_))
}

class PlayerConnection(
  val socket: PlayerSocket,
  val output: Event[Packet]
) extends EventSource[Packet] {

  val printWriters = socket.connect.map { case (_, (_, out)) => out }

  // Send messages.
  val writers = printWriters.foldLeft(Seq[ObjectOutputStream]())((seq, writer) => seq ++ Seq(writer))
  writers.sample(output)
      .foreach { case (msg, writer) => writer.foreach { out => out.writeObject(msg); out.flush() } }

  // Forward messages from input.
  socket.foreach(occur(_))

  // Store message history.
  val history = output.foldLeft(Seq[Packet]())(_ ++ Seq(_))

  history.sample(printWriters)
      .foreach { case (writer, hist) => hist.foreach { msg => writer.writeObject(msg); writer.flush() } }
}
