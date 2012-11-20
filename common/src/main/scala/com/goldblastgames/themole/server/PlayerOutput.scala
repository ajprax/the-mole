package com.goldblastgames.themole.server

import java.io.DataOutputStream

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Behaviour

import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._

class PlayerOutput(
  val name: String,
  val connect: Event[DataOutputStream],
  val output: Event[Packet]
) {

  // Store incoming outputStreams.
  val outputStreams: Behaviour[Seq[DataOutputStream]] = {
    val init: Seq[DataOutputStream] = Seq()
    def combine(outs: Seq[DataOutputStream], out: DataOutputStream) = outs ++ Seq(out)
    
    connect.foldLeft(init)(combine)
  }

  // Send messages.
  outputStreams.sample(output)
      .foreach { case (packet, outs) =>
        outs.foreach { out =>
          try {
            out.writeUTF(serialize(packet))
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
          out.writeUTF(serialize(msg))
          out.flush()
        }
      }
}
