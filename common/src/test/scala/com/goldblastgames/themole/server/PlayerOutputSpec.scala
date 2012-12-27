package com.goldblastgames.themole.server

import java.io.DataOutputStream
import java.io.DataInputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

import org.specs2.ScalaCheck
import org.specs2.mutable._

import com.github.oetzi.echo.TestEvent
import com.goldblastgames.themole.TheMoleSpecification
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._

class PlayerOutputSpec
    extends Specification
    with ScalaCheck
    with TheMoleSpecification {

  trait PlayerOutputTest {
    val connections = new TestEvent[DataOutputStream]
    val packets = new TestEvent[Packet]
    val output = new ByteArrayOutputStream

    val playerOutput = new PlayerOutput("player", connections, packets)
    connections.pubOccur(new DataOutputStream(output))
  }

  "PlayerOutput instances" should {
    "correctly send one packet" in prop { expected: Packet =>
      new PlayerOutputTest {
        packets.pubOccur(expected)

        val ois = new DataInputStream(new ByteArrayInputStream(output.toByteArray))
        val actual = deserialize(ois.readUTF)
        actual mustEqual expected
      }
      ()
    }
    "correctly send multiple packets" in prop { messages: List[Packet] =>
      new PlayerOutputTest {
        messages.foreach(packets.pubOccur(_))

        val ois = new DataInputStream(new ByteArrayInputStream(output.toByteArray))
        messages.foreach(deserialize(ois.readUTF) mustEqual _)
      }
      ()
    }
    "correctly stores packet history" in prop { messages: List[Packet] =>
      new PlayerOutputTest {
        messages.foreach(packets.pubOccur(_))

        messages.zip(playerOutput.history.eval)
            .foreach { case (a, b) => a mustEqual b }
      }
      ()
    }
    "correctly send history upon connection" in prop { messages: List[Packet] =>
      new PlayerOutputTest {
        messages.foreach(packets.pubOccur(_))

        val output2 = new ByteArrayOutputStream
        connections.pubOccur(new DataOutputStream(output2))

        val ois = new DataInputStream(new ByteArrayInputStream(output.toByteArray))
        val ois2 = new DataInputStream(new ByteArrayInputStream(output2.toByteArray))
        messages.foreach(deserialize(ois.readUTF) mustEqual _)
        messages.foreach(deserialize(ois2.readUTF) mustEqual _)
      }
      ()
    }
  }
}
