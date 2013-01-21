package com.goldblastgames.themole.server

import java.io.EOFException
import java.io.DataInputStream

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import unfiltered.netty.websockets.WebSocket

import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._

class PlayerInput(
  val name: String,
  val connect: Event[(WebSocket, Option[String])]
) extends EventSource[Packet] {

  connect.foreach {
    case (s, strOpt) =>
    {
      val thread = new Thread(new Runnable() {
        def run() {
          in.map({case (x,y) => occur(deserialize(y))})
        }
      })
      thread.start()
    }
  }

  // Store received packets.
  val history: Behaviour[Seq[Packet]] = {
    val init: Seq[Packet] = Seq()
    def combine(packets: Seq[Packet], packet: Packet) = packets ++ Seq(packet)

    this.foldLeft(init)(combine)
  }
}

class ClientPlayerInput(
  val name: String,
  val connect: Event[DataInputStream]
) extends EventSource[Packet] {

  connect.foreach { in: DataInputStream =>

    val thread = new Thread(new Runnable() {
      def run() {
        Iterator
            .continually({
              try {
                in.readUTF()
              } catch {
                case ex: EOFException => null
              }
            })
            .takeWhile(_ != null)
            .collect({ case x: String => deserialize(x) })
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

