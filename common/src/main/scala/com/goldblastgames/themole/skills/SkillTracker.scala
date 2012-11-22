package com.goldblastgames.themole.skills

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Constant
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Stepper

import com.goldblastgames.themole.io.SubmitCommand
import com.goldblastgames.themole.Player
import com.goldblastgames.themole.mission._
import com.goldblastgames.themole.Nation
import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._
import com.goldblastgames.themole.mission.MissionDebriefing
import com.goldblastgames.themole.mission.MissionResult._
import com.goldblastgames.themole.mission.MissionResult

class SkillTracker(sources: Map[Player, Event[SubmitCommand]], missionTracker: MissionTracker) {

  // A behaviour[int] that is always zero
  val zero: Behaviour[Int] = new Constant(0)

  // Dummy behaviours for debriefing level
  val one: Behaviour[Int] = new Constant(1)
  val debriefingLevels: Map[Player, Behaviour[Int]] = {
    sources.keys.map {
      case player => {
        player -> one
        }
      }.toMap
    }

  // Behaviours for each skill of each player
  val submittedSkills: Map[Nation, Map[Skill, Map[Player, Behaviour[Int]]]] =
    Nation.values.map {
      case nation => {
        nation -> {
          Skills.values.map {
            case skill => {
              skill -> {
                sources.map{
                  case (player, event) => {
                    val filteredSubmissionAmount: Event[Int] = event
                      .filter(submit => submit.camp == nation)
                      .filter(submit => submit.skill == skill)
                      .map((t, submission) => submission.amount)

                    player -> new Stepper[Int](0, filteredSubmissionAmount)
                  }
                }
              }
            }
          }.toMap
        }
      }
    }.toMap

  // Behaviours for totals of skills
  // Separated by mission
  // used in debriefing level two
  val totalsBySkill: Map[Nation, Map[Skill, Behaviour[Int]]] =
    submittedSkills.map {
      case (camp, skillsToSubs) => {
        camp -> {
          skillsToSubs.map {
            case (skill, submissions) => {
              skill -> {
                submissions.values.foldLeft(zero)(_.map2(_)((x, y) => x + y))
              }
            }
          }
        }
      }
    }

  // Behaviours for totals submissions by player
  // Not separated by mission
  // used in debriefing level three
  val totalsByPlayer: Map[Player, Behaviour[Int]] =
    sources.map {
      case (player, event) => {
        player -> {
          val submissionsList = for {
            camp <- submittedSkills.keys
            skill <- submittedSkills(camp).keys
            submitter <- submittedSkills(camp)(skill).keys
            if (submitter == player)
            } yield submittedSkills(camp)(skill)(submitter)
          submissionsList.foldLeft(zero)(_.map2(_)((x, y) => x + y))
        }
      }
    }

  // Behaviours for totals by skill separated by mission and camp of submitter
  // used in debriefing level four
  // the outermost Map checks against the submitters camp,
  // everything within follows the format of totalsBySkill
  val totalsBySkillByCamp: Map[Nation, Map[Nation, Map[Skill, Behaviour[Int]]]] =
    Nation.values.map {
      case nation => {
        nation -> {
          submittedSkills.map {
            case (camp, skillsToSubs) => {
              camp -> {
                skillsToSubs.map {
                  case (skill, submissions) => {
                    skill -> {
                      submissions.filter(
                        (submission: (Player, Behaviour[Int])) => submission._1.camp == nation).
                        values.
                        foldLeft(zero)(_.map2(_)((x, y) => x + y))
                    }
                  }
                }.toMap
              }
            }
          }.toMap
        }
      }
    }.toMap

  // Result depends on previous mission's skill requirements and the totals behaviours
  val prevResults: Behaviour[Tuple2[MissionResult, MissionResult]] = missionTracker.prevMissions.map(evaluateResult(_))

  def findMargins(mission: Mission): (Int, Int) =
    (sum(mission.camp, mission.primaryObjective.positiveSkills) - sum(mission.camp, mission.primaryObjective.negativeSkills),
     sum(mission.camp, mission.secondaryObjective.positiveSkills) - sum(mission.camp, mission.secondaryObjective.negativeSkills))

  // totals the submission values for a list of skills
  def sum(nation: Nation, skills: List[Skill]): Int = {
    val submissionsList = for {
      submittedSkill <- skills
      camp <- submittedSkills.keys
      skill <- submittedSkills(camp).keys
      submitter <- submittedSkills(camp)(skill).keys
      if (submittedSkill == skill)
      if (camp == nation)
      } yield submittedSkills(camp)(skill)(submitter).eval
    submissionsList.reduce(_ + _)
    }

  def evaluateResult(missions: Tuple2[Mission, Mission]): Tuple2[MissionResult, MissionResult] = {

    def singleMission(mission: Mission): MissionResult = {
      if (mission == null) (true, true) else {
      val primaryObjective = mission.primaryObjective
      val secondaryObjective = mission.secondaryObjective
      val margins = findMargins(mission)
      val primary = primaryObjective.difficulty < margins._1
      val secondary = {
        if (secondaryObjective.linked && secondaryObjective.opposed) primary && (secondaryObjective.difficulty < margins._2)
        else if (secondaryObjective.linked && !secondaryObjective.opposed)
          primary && (secondaryObjective.difficulty < sum(mission.camp, secondaryObjective.positiveSkills) - 6)
        else if (!secondaryObjective.linked && secondaryObjective.opposed) secondaryObjective.difficulty < margins._2
        else secondaryObjective.difficulty < (sum(mission.camp, secondaryObjective.positiveSkills) - 6)
        }
      (primary, secondary)}
      }
    (singleMission(missions._1), singleMission(missions._2))
  }

  val prevDebriefings: Behaviour[Tuple2[List[MissionDebriefing], List[MissionDebriefing]]] = missionTracker.prevMissions.map(getDebriefings(_))

  def getDebriefings(missions: Tuple2[Mission, Mission]) = {
    def singleCamp(mission: Mission): List[MissionDebriefing] = {
      if (mission == null) List(new DebriefingDummy, new DebriefingDummy)
      else List(
      new DebriefingLevelOne(mission.camp, mission.day, findMargins(mission)._1, findMargins(mission)._2),
      new DebriefingLevelTwo(mission.camp, totalsBySkill),
      new DebriefingLevelThree(mission.camp, totalsByPlayer),
      new DebriefingLevelFour(mission.camp, totalsBySkillByCamp),
      new DebriefingLevelFive(mission.camp, submittedSkills)
      )
      }
    (singleCamp(missions._1), singleCamp(missions._2))
  }

}

