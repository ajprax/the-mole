package com.goldblastgames.server

import org.specs2.ScalaCheck
import org.specs2.mutable._

import com.github.oetzi.echo.TestEvent
import com.goldblastgames.GameServerSpecification

class PlayerInputSpec
    extends Specification
    with ScalaCheck
    with GameServerSpecification {

  trait PlayerInputTest {
    val connections = new TestEvent[ObjectInputStream]

    connections.pubOccur(new ObjectInputStream(
  }

  "PlayerInput instances" should {
    "correctly receive one packet" in prop { expected: Packet =>
      
}
