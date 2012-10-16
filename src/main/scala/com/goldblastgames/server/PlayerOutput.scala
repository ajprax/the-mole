package com.goldblastgames.server

import java.io.ObjectOutputStream

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Behaviour

import com.goldblastgames.io.Packet

class PlayerOutput(
  val name: String,
  val connect: Event[ObjectOutputStream],
  val output: Event[Packet]
) {

  // Store incoming outputStreams.
  val outputStreams: Behaviour[Seq[ObjectOutputStream]] = {
    val init: Seq[ObjectOutputStream] = Seq()
    def combine(outs: Seq[ObjectOutputStream], out: ObjectOutputStream) = outs ++ Seq(out)
    
    connect.foldLeft(init)(combine)
  }

  // Send messages.
  outputStreams.sample(output)
      .foreach { case (packet, outs) =>
        outs.foreach { out =>
          try {
            out.writeObject(packet)
            out.flush()
          } catch {
            case _ =>
          }
        }
      }

  // Store outgoing packets.
  val history: Behaviour[Seq[Packet]] = {
    val init: Seq[Packet] = Seq()
    def combine(packets: Seq[Packet], packet: Packet) = packets ++ Seq(packet)

    output.foldLeft(init)(combine)
  }

  // Re-send message history for each connection.
  history.sample(connect)
      .foreach { case (out, hist) =>
        hist.foreach { msg =>
          out.writeObject(msg)
          out.flush()
        }
      }
}
