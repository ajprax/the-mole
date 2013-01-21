package com.goldblastgames.themole.mission

import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._
import com.goldblastgames.themole.skills.Skills

case class Mission(
  camp: Nation,
  day: Int,
  primaryObjective: PrimaryObjective,
  secondaryObjective: SecondaryObjective
) {

  override def toString: String = {
    val lineOne = "%s Mission #%s:".format(camp, day)
    val primary = primaryObjective
    val secondary = secondaryObjective
    "%s\n%s\n%s".format(lineOne, primary, secondary)
    }
}

object Mission {
  val generator = new MissionGenerator
  def nextMissions = generator.next
}

class MissionGenerator {
  import scala.util.Random
  private var day: Int = 0
  val random = new Random()
  def getDifficulty = {
    val baseDifficulty = if (day < 10) (day / 2).toInt else 5 // slowly increment difficulty
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
    val shuffledSkills =
      random.shuffle(
        List(
          random.shuffle(List(Subterfuge, InformationGathering)),
          random.shuffle(List(Wetwork, Sabotage)),
          random.shuffle(List(Sexitude, Stoicism))
        )
      )
    List(shuffledSkills(0)(0), shuffledSkills(1)(0), shuffledSkills(2)(0))
  }
  def getRewards(camp: Nation) = { // all rewards are the same probability at all times for now
    val missionRewards = List(
      new MissionReward(camp, "Points", Some(5)),
      new MissionReward(camp, "Points", Some(10)),
      new MissionReward(camp, "Points", Some(15)),
      new MissionReward(camp, "Debriefing Detail", Some(1)),
      new MissionReward(camp, "Debriefing Detail", Some(2))
    )
    val shuffledRewards = random.shuffle(missionRewards)
    List(shuffledRewards(0), shuffledRewards(1))
  }
  def next = {
    day += 1
    val (difficultyAmerica, difficultyUSSR) = (getDifficulty, getDifficulty)
    val (skillsAmerica, skillsUSSR) = (getSkills, getSkills)
    val (rewardsAmerica, rewardsUSSR) = (getRewards(America), getRewards(USSR))
    (new Mission(America, day, PrimaryObjective(skillsAmerica, difficultyAmerica, primaryType, rewardsAmerica(0)), SecondaryObjective(skillsAmerica, difficultyAmerica, linked, opposed, rewardsAmerica(1))),
    new Mission(USSR, day, PrimaryObjective(skillsUSSR, difficultyUSSR, primaryType, rewardsUSSR(0)), SecondaryObjective(skillsUSSR, difficultyUSSR, linked, opposed, rewardsUSSR(1))))
  }
}
