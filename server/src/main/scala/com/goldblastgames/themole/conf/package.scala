package com.goldblastgames.themole

import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.XML

import shapeless.Field

import com.goldblastgames.themole.chat.ChatEffect
import com.goldblastgames.themole.skills.Skills

package object conf {
  object portField        extends Field[Int]
  object playersField     extends Field[Seq[Player]]
  object dropFreqField    extends Field[Long]
  object missionFreqField extends Field[Long]
  object effectsField     extends Field[Seq[ChatEffect]]

  def readXml(filename: String = "game.xml") = XML.loadFile(filename)

  def readPort(root: NodeSeq) = (portField -> root.text.toInt)

  def readPlayers(root: NodeSeq) = {
    val playerNodes = root \ "player"
    val players: Seq[Player] = playerNodes.map { playerNode =>
      Player(
        (playerNode \ "@name").text,
        Nation.withName((playerNode \ "@camp").text),
        Nation.withName((playerNode \ "@allegiance").text),
        // Dummy skills
        Map((Skills.withName("InformationGathering") -> (1,10)),
          (Skills.withName("Subterfuge") -> (1,10)),
          (Skills.withName("Wetwork") -> (1,10)),
          (Skills.withName("Sabotage") -> (1,10)),
          (Skills.withName("Sexitude") -> (1,10)),
          (Skills.withName("Stoicism") -> (1,10))
        )
      )
    }

    (playersField -> players)
  }

  def readDropFreq(root: NodeSeq) = (dropFreqField -> root.text.toLong)

  def readMissionFreq(root: NodeSeq) = (missionFreqField -> root.text.toLong)
}
