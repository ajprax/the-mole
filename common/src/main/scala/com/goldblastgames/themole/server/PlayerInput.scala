package com.goldblastgames.themole.server

import java.io.EOFException
import java.io.ObjectInputStream

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

import com.goldblastgames.themole.io.Packet

class PlayerInput(
  val name: String,
  val connect: Event[ObjectInputStream]
) extends EventSource[Packet] {

  connect.foreach { in: ObjectInputStream =>

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

  // Store received packets.
  val history: Behaviour[Seq[Packet]] = {
    val init: Seq[Packet] = Seq()
    def combine(packets: Seq[Packet], packet: Packet) = packets ++ Seq(packet)

    this.foldLeft(init)(combine)
  }
}
