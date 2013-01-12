package com.goldblastgames.themole.skills

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Constant
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Stepper
import com.github.oetzi.echo.Echo._

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

  // Behaviours for each submitted skill of each player
  val submittedSkills: Map[Nation, Map[Skill, Map[Player, Behaviour[Int]]]] =
    Nation.values.map {
      case nation => {
        nation -> {
          Skills.values.map {
            case skill => {
              skill -> {
                sources.map{
                  case (player, event) => {
                    // an Event[Int] with occurences at the same times as TimedMissionChange
                    // always valued at 0 to reset submissions
                    val resetSubmissionAmount: Event[Int] = missionTracker.missionEvent
                      .map((_, _) => 0)
                    val filteredSubmissionAmount: Event[Int] = event
                      .filter(submit => submit.camp == nation)
                      .filter(submit => submit.skill == skill)
                      .filter(submit => submit.amount <= currentSkills(player)(skill).eval())
                      .map((t, submission) => submission.amount)
                    val currentValue = filteredSubmissionAmount merge resetSubmissionAmount
                    player -> currentValue.foldLeft((0, 0))((prev: (Int, Int), curr: Int) => (prev._2, curr)).map(tup => tup._1)
                  }
                }
              }
            }
          }.toMap
        }
      }
    }.toMap

  // Behaviours for skill submissions by player independent of mission
  // used by player skill tracking, not in debriefings
  val submissionsByPlayerBySkill: Map[Player, Map[Skill, Behaviour[Int]]] =
    sources.map {
      case (player, event) => {
        player -> {
          Skills.values.map {
            case skill => {
              val filteredSubmissionAmount: Event[Int] = event
                .filter(submit => submit.skill == skill)
                .filter(submit => submit.amount <= currentSkills(player)(skill).eval())
                .map((t, submission) => submission.amount)

                skill -> new Stepper(0, filteredSubmissionAmount)
            }
          }.toMap
        }
      }
    }.toMap

  // Current value of each player's skills
  val currentSkills: Map[Player, Map[Skill, Behaviour[Int]]] =
    sources.keys.map {
      case player => {
        player -> {
          Skills.values.map {
            case skill => {
              skill -> submissionsByPlayerBySkill(player)(skill)
                .sample(missionTracker.missionEvent)
                .map((time: Time, tuple: ((Mission, Mission), Int)) => tuple._2)
                .merge(missionTracker.missionEvent.map((_, _) => -2))
                .foldLeft(player.skillsRanges(skill)._2)((current: Int, next: Int) => {
                  if (current - next < player.skillsRanges(skill)._1)
                    if (current - next >= 0)
                      player.skillsRanges(skill)._1
                    else current
                  else if (current - next > player.skillsRanges(skill)._2)
                    current
                  else current - next
                })
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
                        (submission: (Player, Behaviour[Int])) => submission._1.camp == nation)
                        .values.foldLeft(zero)(_.map2(_)((x, y) => x + y))
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
  val prevResults: Behaviour[Tuple2[MissionResult, MissionResult]] = missionTracker.prevMissions.map(evaluateResults(_))

  def evaluateResults(missions: (Mission, Mission)): (MissionResult, MissionResult) = {
    val (americaSuccess, ussrSuccess) = evaluateSuccess(missions)
    val (americaDebriefings, ussrDebriefings) = getDebriefings(missions)
    (new MissionResult(americaSuccess, americaDebriefings), new MissionResult(ussrSuccess, ussrDebriefings))
    }

  // Subtract negative skill totals from positive skill totals
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

  def evaluateSuccess(missions: Tuple2[Mission, Mission]): ((Boolean, Boolean), (Boolean, Boolean)) = {

    def singleMission(mission: Mission): (Boolean, Boolean) = {
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

