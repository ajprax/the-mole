package com.goldblastgames

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import com.github.oetzi.echo.EchoSpecification
import com.goldblastgames.io.Connect
import com.goldblastgames.io.DeadDrop
import com.goldblastgames.io.Packet
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.skills.Skills
import com.goldblastgames.skills.Skills._

trait GameServerSpecification extends EchoSpecification {

  implicit val arbSkill: Arbitrary[Skill] = Arbitrary {
    Gen.oneOf(InformationGathering, Wetwork, Sabotage, Sexitude, Stoicism)
  }

  implicit val arbConnect: Arbitrary[Connect] = Arbitrary {
    for {
      name <- arbitrary[String]
    } yield Connect(name)
  }

  implicit val arbDeadDrop: Arbitrary[DeadDrop] = Arbitrary {
    for {
      text <- arbitrary[String]
    } yield DeadDrop(text)
  }

  implicit val arbMessage: Arbitrary[Message] = Arbitrary {
    for {
      sender <- arbitrary[String]
      channel <- arbitrary[String]
      body <- arbitrary[String]
    } yield Message(sender, channel, body)
  }

  implicit val arbSubmitCommand: Arbitrary[SubmitCommand] = Arbitrary {
    for {
      sender <- arbitrary[String]
      skill <- arbitrary[Skill]
      amount <- arbitrary[Int]
    } yield SubmitCommand(sender, skill, amount)
  }

  implicit val arbPacket: Arbitrary[Packet] = Arbitrary {
    Gen.oneOf(
      arbitrary[Connect],
      arbitrary[DeadDrop],
      arbitrary[Message],
      arbitrary[SubmitCommand])
  }
}
