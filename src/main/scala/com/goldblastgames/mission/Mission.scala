package com.goldblastgames.mission

import com.goldblastgames.Nation._

object Skills extends Enumeration {
  type Skill = Value
  val Subterfuge,
    InformationGathering,
    Wetwork,
    Sabotage,
    Sexitude,
    Stoicism = Value
}

case class SkillRequirement(skill: Skills.Skill, min: Int) {
  def humanString(): String = {
    "%s: %s".format(skill, min)
  }
}

object Rewards extends Enumeration {
  type Reward = Value
  val MRD, points = Value
}

case class MissionObjective(
  primary: SkillRequirement, 
  secondary: Option[SkillRequirement], 
  reward: Rewards.Reward
) {
  def humanString(): String = {
    "Skill 1: %s\n Skill2: %s\n Reward:".format(
      primary.humanString,
      if (secondary.isEmpty) { "None" } else {secondary.get.humanString},
      reward.toString
    )
  }
}

case class Mission(
  id: Int,
  team: Nation,
  primaryObjective: MissionObjective,
  secondaryObjective: MissionObjective,
  description: String
) {
  override def toString(): String = {
    ("Mission #%s for Team %s: \n" +
    "Primary Objective requires: %s \n" +
    "Secondary Objective requires: %s \n" +
    "Mission description: %s").format(
      id,
      team.toString, 
      primaryObjective.humanString, 
      secondaryObjective.humanString, 
      description
    )
  }
}

object Mission {
  // TODO(Issue-36): actually generate missions
  val dummyMission = 
    new Mission(System.currentTimeMillis.toInt,
      America, 
      new MissionObjective(
        new SkillRequirement(Skills.InformationGathering, 2), 
        Some(new SkillRequirement(Skills.Stoicism, 1)),
        Rewards.MRD
      ),
      new MissionObjective(
        new SkillRequirement(Skills.Stoicism, 3),
        None,
        Rewards.points
      ),
      "Dummy mission alert"
    )
}
