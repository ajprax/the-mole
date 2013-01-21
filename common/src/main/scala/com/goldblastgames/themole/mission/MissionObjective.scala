package com.goldblastgames.themole.mission

import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.skills.Skills._

abstract class MissionObjective
case class PrimaryObjective(
  skills: List[Skill],
  difficulty: Int,
  andOrSingle: String,
  reward: MissionReward
  ) extends MissionObjective {

  val (positiveSkills, negativeSkills) = {
    if (andOrSingle == "single") (List(skills(0)), List(Skills.skillPairs(skills(0))))
    else (List(skills(0), skills(1)), List(Skills.skillPairs(skills(0)), Skills.skillPairs(skills(0))))
    }

  override def toString = {
    val lineOneA = if (andOrSingle == "AND") "and"
                  else if (andOrSingle == "OR") "or"
                  else ""
    val lineOneB = if (andOrSingle == "single") "" else skills(1)
    val lineOne = "Primary Objective requires: %s %s %s".format(skills(0), lineOneA, lineOneB)
    val lineTwo = "  difficulty: exceed the enemy by: " + difficulty
    val lineThree = "  reward: " + reward
    "%s\n%s\n%s".format(lineOne, lineTwo, lineThree)
    }
  }
case class SecondaryObjective(
  skills: List[Skill],
  difficulty: Int,
  linked: Boolean,
  opposed: Boolean,
  reward: MissionReward
  ) extends MissionObjective {
  val (positiveSkills, negativeSkills) = (List(skills(2)), List(Skills.skillPairs(skills(2))))
  override def toString = {
    val lineOneA = if (linked) " and completion of the Primary Objective" else ""
    val lineOne = "Secondary Objective requires: " + skills(2) + lineOneA
    val lineTwo = if (opposed) " difficulty: exceed the enemy by: " + difficulty
                  else "  diffculty: exceed %s in total submissions".format((difficulty + 6))
    val lineThree = "  rewards: " + reward
    "%s\n%s\n%s".format(lineOne, lineTwo, lineThree)
    }
  }
