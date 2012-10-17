package com.goldblastgames.themole.server

import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

import org.apache.commons.lang3.SerializationUtils
import org.specs2.ScalaCheck
import org.specs2.mutable._

import com.github.oetzi.echo.TestEvent
import com.goldblastgames.themole.TheMoleSpecification
import com.goldblastgames.themole.io.Packet

class PlayerOutputSpec
    extends Specification
    with ScalaCheck
    with TheMoleSpecification {

  trait PlayerOutputTest {
    val connections = new TestEvent[ObjectOutputStream]
    val packets = new TestEvent[Packet]
    val output = new ByteArrayOutputStream

    val playerOutput = new PlayerOutput("player", connections, packets)
    connections.pubOccur(new ObjectOutputStream(output))
  }

  "PlayerOutput instances" should {
    "correctly send one packet" in prop { expected: Packet =>
      new PlayerOutputTest {
        packets.pubOccur(expected)

        val actual = SerializationUtils.deserialize(output.toByteArray)
        actual mustEqual expected
      }
      ()
    }
    "correctly send multiple packets" in prop { messages: List[Packet] =>
      new PlayerOutputTest {
        messages.foreach(packets.pubOccur(_))

        val ois = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray))
        messages.foreach(ois.readObject mustEqual _)
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
        connections.pubOccur(new ObjectOutputStream(output2))

        val ois = new ObjectInputStream(new ByteArrayInputStream(output.toByteArray))
        val ois2 = new ObjectInputStream(new ByteArrayInputStream(output2.toByteArray))
        messages.foreach(ois.readObject mustEqual _)
        messages.foreach(ois2.readObject mustEqual _)
      }
      ()
    }
  }
}
