package com.goldblastgames.themole.mission

import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._

case class SkillRequirement(skill: Skill, min: Int) {
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
  successFunc: (Boolean, Option[Boolean]) => Boolean, 
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
  camp: Nation,
  day: Int,
  difficulty: Int,
  linked: Boolean,
  opposed: Boolean,
  primaryType: String,
  skills: List[String],
  rewards: List[List[String]]
) {

  override def toString: String = {
    val lineOne = "Mission #%s:".format(day)
    val lineTwoA = if (primaryType == "AND") "and"
                   else if (primaryType == "OR") "or"
                   else ""
    val lineTwoB = if (primaryType == "single") "" else skills(1)
    val lineTwo = "Primary Objective requires: %s %s %s".format(skills(0), lineTwoA, lineTwoB)
    val lineThree = "  difficulty: exceed the enemy by: " + difficulty
    val lineFour = "  rewards: " + rewards(0)(0)
    val lineFiveA = if (linked) " and completion of the Primary Objective" else ""
    val lineFive = "Secondary Objective requires: " + skills(2) + lineFiveA
    val lineSix = if (opposed) "  difficulty: exceed the enemy by: " + difficulty else "  difficulty: exceed %s in total submissions".format((difficulty + 6))
    val lineSeven = "  rewards: " + rewards(0)(1)
    "%s\n%s\n%s\n%s\n%s\n%s\n%s".format(lineOne, lineTwo, lineThree, lineFour, lineFive, lineSix, lineSeven)
  }
}
/*
case class Mission(
  id: Int,
  team: Nation,
  primaryObjective: MissionObjective,
  secondaryObjective: MissionObjective,
  description: String
) {
  def humanString(): String = {
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
*/
case class MissionResult(
  mission: Mission,
  success: Boolean,
  level1: Option[Debriefing1],
  level2: Option[Debriefing2],
  body: String
) {
  def humanString(): String = {
    "\n%s \nSubmitted Skills \n%s".format(mission, body)
  }
}

// TODO: Actual mission debriefing detail.
case class Debriefing1(
  margins: Map[MissionObjective, Int]
)

case class Debriefing2(
  submitted: Map[Skill, Int]
)

object Mission {
  
  val generator = new AaronGenerator
  def nextMission = generator.next

}

class AaronGenerator {
  import scala.util.Random
  private var day: Int = 0
  val random = new Random()
  def getDifficulty = {
    val baseDifficulty = if (day < 10) (day / 2).toInt else 5 // slowly increment difficulty
    // this is a little longer than it really needs to be, but this way we can easily adjust the range of difficulties
    val difficultyScale: Array[Double] = Array(-1.5, -0.5, 0.5, 1.5)
    val difficultyRandomized = random.nextGaussian
    def minimum(x: Int): Int = if (x > 1) x else 1 // prevents impossible difficulties
                                                   // maximum is already enforced by baseDifficulty
    if (difficultyRandomized < difficultyScale(0))
      minimum(baseDifficulty - 2)
    else if (difficultyRandomized < difficultyScale(1))
      minimum(baseDifficulty - 1)
    else if (difficultyRandomized < difficultyScale(2))
      minimum(baseDifficulty)
    else if (difficultyRandomized < difficultyScale(3))
      minimum(baseDifficulty + 1)
    else
      minimum(baseDifficulty + 2)
    }
  def linked = random.nextInt(10) > 5 // does secondary objective require primary
  def opposed = random.nextInt(3) == 0 // is secondary objective relative or absolute
  def primaryType = {
    val primaryRandomized = random.nextInt(4)
    if (primaryRandomized < 2) "single"
    else if (primaryRandomized < 3) "AND"
    else "OR"
    }
  // getSkills works, but it's pretty unweildy
  def getSkills = {
    val shuffledSkills = random.shuffle(List(random.shuffle(List("Subterfuge", "Information Gathering")), random.shuffle(List("Wetwork", "Sabotage")), random.shuffle(List("Sexitude", "Stocism"))))
    List(shuffledSkills(0)(0), shuffledSkills(1)(0), shuffledSkills(2)(0))
    }
  def getRewards = { // all rewards are the same probability at all times for now
    val missionRewards = List(
      "10 points",
      "Negative communication effect for the enemy camp",
      "The enemy camp will suffer a crisis in the next 48 hours",
      "Reset the cooldown of all abilities of players in your camp",
      "Your camp votes for a player to receive a special ability",
      "Double the positive effects of your next mission",
      "5 points",
      "15 points",
      "Mission debriefing detail +1",
      "Missiong debriefing detail +2",
      "A random player in your camp gets +1 to a skill minimum"
      )
    val shuffledRewards = random.shuffle(missionRewards)
    val negativeComms = List(
      "Random words are redacted from enemy messages for 24 hours",
      "Enemy camp private messages are sent to random recipients in the same camp for 24 hours",
      "Enemy camp cannot send private messages for 24 hours",
      "Enemy camp messages are anonymous for 24 hours",
      "Enemy camp does not receive dead drop messages for 18 hours"
      )
    val shuffledNegativeComms = random.shuffle(negativeComms)
    val specialAbilities = List(
      "View one players allegiance in the next 24 hours",
      "During the next mission or crisis your skill use is free, must be used within 72 hours",
      "Choose between two options for your next mission"
      )
    val shuffledSpecialAbilities = random.shuffle(specialAbilities)
    val selectedRewards = List(shuffledRewards(0), shuffledRewards(1))
    val possibleRewardDetails = List(
      shuffledNegativeComms(0),
      shuffledNegativeComms(1),
      shuffledSpecialAbilities(0),
      shuffledSpecialAbilities(1)
      )
    def matchRewards(rewardNumber: Int) = {
      if (selectedRewards(rewardNumber) == "Negative communication effect for the enemy camp")
        possibleRewardDetails(rewardNumber)
      else if (selectedRewards(rewardNumber) == "Your camp votes for a player to receive a special ability")
        possibleRewardDetails(rewardNumber + 2)
      else
        "none"
      }
    List(selectedRewards, List(matchRewards(0), matchRewards(1)))
    }
  def allInOne =
    List(getDifficulty, linked, opposed, primaryType, getSkills, getRewards)
  def next = {
    day += 1
    (new Mission(America, day, getDifficulty, linked, opposed, primaryType, getSkills, getRewards),
    new Mission(USSR, day, getDifficulty, linked, opposed, primaryType, getSkills, getRewards))
  }
}
