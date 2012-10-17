package com.goldblastgames.themole

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import com.github.oetzi.echo.EchoSpecification
import com.goldblastgames.themole.io.Connect
import com.goldblastgames.themole.io.DeadDrop
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.Message
import com.goldblastgames.themole.io.SubmitCommand
import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.skills.Skills._

trait TheMoleSpecification extends EchoSpecification {

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
