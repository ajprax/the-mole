package com.goldblastgames

import com.goldblastgames.Nation._

object Skills extends Enumeration {
  type skill = Value
  val Subterfuge,
    InformationGathering,
    Wetwork,
    Sabotage,
    Sexitude,
    Stoicism = Value
}

case class SkillRequirement(skill: Skills.skill, min: Int) {
  override def toString(): String = {
    "%s: %s".format(skill, min)
  }
}

object Rewards extends Enumeration {
  type reward = Value
  val MRD, points = Value
}

case class MissionObjective(
  primary: SkillRequirement, 
  secondary: SkillRequirement, 
  reward: Rewards.reward
) {
  override def toString(): String = {
    "Skill 1: %s\n Skill2: %s\n Reward:".format(
      primary.toString,
      secondary.toString,
      reward.toString
    )
  }
}

// TODO Resolve missions based on submitted skills.
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
      primaryObjective.toString, 
      secondaryObjective.toString, 
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

