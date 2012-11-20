package com.goldblastgames.themole.io

import org.specs2.ScalaCheck
import org.specs2.mutable._

import com.goldblastgames.themole.TheMoleSpecification
import com.goldblastgames.themole.io.PacketSerialization._

class SerializationSpec
  extends Specification
  with ScalaCheck
  with TheMoleSpecification {

  sequential

  "Serialization" should {
    "deserialize to the same" in prop {
      expected: Packet => {
        deserialize(serialize(expected)) mustEqual expected
      }
    }
  }

}
