package com.goldblastgames.themole.mission

import com.github.oetzi.echo.core.Behaviour

import com.goldblastgames.themole.Nation
import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.Player
import com.goldblastgames.themole.skills._
import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.skills.Skills._

abstract class MissionDebriefing {
  val level: Int
  }

class DebriefingDummy extends MissionDebriefing {
  val level = 0
  override def toString = "dummy debriefing"
  }

case class DebriefingLevelOne (camp: Nation, day: Int, primaryMargin: Int, secondaryMargin: Int) extends MissionDebriefing {
  val level = 1
  override def toString = {
    val lineOne = "%s Mission #%s".format(camp, day)
    val lineTwo = {
      if (primaryMargin > 0) "Primary objective passed by %s".format(primaryMargin)
      else "Primary objective failed by %s".format(primaryMargin * -1)
      }
    val lineThree = {
      if (secondaryMargin > 0) "Secondary objective passed by %s".format(secondaryMargin)
      else "Secondaryobjective failed by %s".format(secondaryMargin * -1)
      }
    "%s\n%s\n%s".format(lineOne, lineTwo, lineThree)
    }
  }

case class DebriefingLevelTwo (camp: Nation, totalsBySkill: Map[Nation, Map[Skill, Behaviour[Int]]]) extends MissionDebriefing {
  val level = 2
  override def toString = {
    val submissionTotals = for {
      nation <- totalsBySkill.keys
      skill <- totalsBySkill(nation).keys
      if (camp == nation)
      } yield (skill, totalsBySkill(nation)(skill).eval)
    "\nTotal amount of each skill submitted to your camp's mission:\n" +
      submissionTotals.map {
        case (skill, submissionValue) =>
          skill.toString + ": " + submissionValue
        }.reduce(_ + "\n" + _)
      }
    }

case class DebriefingLevelThree (camp: Nation, totalsByPlayer: Map[Player, Behaviour[Int]]) extends MissionDebriefing {
  val level = 3
  override def toString = {
    val playerTotals = for {
      player <- totalsByPlayer.keys
      if (player.camp == camp)
      } yield (player, totalsByPlayer(player))
    "\nTotal amount submitted by each player in your camp:\n" +
      playerTotals.map {
        case (player, submissionValue) =>
          player.name + ": " + submissionValue.eval
        }.reduce(_ + "\n" + _)
    }
  }

case class DebriefingLevelFour (camp: Nation, totalsBySkillByCamp: Map[Nation, Map[Nation, Map[Skill, Behaviour[Int]]]]) extends MissionDebriefing {
  val level = 4
  override def toString = {
    val submissionTotals = for {
      nation <- totalsBySkillByCamp.keys
      missionCamp <- totalsBySkillByCamp(nation).keys
      skill <- totalsBySkillByCamp(nation)(missionCamp).keys
      if (nation == camp)
      } yield (skill, totalsBySkillByCamp(nation)(missionCamp)(skill).eval)
    "\nTotal of each skill submitted by players in your camp:\n" +
      submissionTotals.map {
        case (skill, submissionValue) =>
          skill.toString + ": " + submissionValue
        }.reduce(_ + "\n" + _)
    }
  }

case class DebriefingLevelFive (camp: Nation, submittedSkills: Map[Nation, Map[Skill, Map[Player, Behaviour[Int]]]]) extends MissionDebriefing {
  val level = 5
  override def toString = {
    val submissionTotals = for {
      nation <- submittedSkills.keys
      skill <- submittedSkills(nation).keys
      player <- submittedSkills(nation)(skill).keys
      if (player.camp == camp)
      } yield (player, skill, submittedSkills(nation)(skill)(player).eval)
    val filtered = submissionTotals.filter((submission: (Player, Skill, Int)) => submission._3 != 0)
    val formatted: List[String] = filtered.map {
          case (player, skill, submissionValue) =>
            player.name + ": " + skill.toString + ": " + submissionValue
          }.toList
    if (formatted != Nil) "\nTotal of each skill submitted by each player in your camp to your camp's mission:\n" +
      formatted.reduce(_ + "\n" + _)
    else "No skills submitted by players in your camp to your camp's missions."
    }
  }
