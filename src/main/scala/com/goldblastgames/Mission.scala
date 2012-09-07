package com.goldblastgames

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
  secondary: SkillRequirement, 
  reward: Rewards.Reward
) {
  def humanString(): String = {
    "Skill 1: %s\n Skill2: %s\n Reward:".format(
      primary.humanString,
      secondary.humanString,
      reward.toString
    )
  }
}

// TODO(issue-33): Resolve missions based on submitted skills.
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

object MissionGenerator {
  // not threadsafe...
  var counter = 0

  def nextMission(): Mission = {
    counter += 1
    new Mission(counter,
      Nation.America, 
      new MissionObjective(
        new SkillRequirement(Skills.InformationGathering, 2), 
        new SkillRequirement(Skills.Stoicism, 1),
        Rewards.MRD
      ),
      new MissionObjective(
        new SkillRequirement(Skills.Stoicism, 3),
        new SkillRequirement(Skills.Wetwork, 2),
        Rewards.points
      ),
      "Dummy mission alert"
    )
  }
}

