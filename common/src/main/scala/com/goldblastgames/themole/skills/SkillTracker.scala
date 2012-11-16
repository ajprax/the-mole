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
import com.goldblastgames.themole.mission.MissionResult._
import com.goldblastgames.themole.mission.MissionResult

class SkillTracker(sources: Map[Player, Event[SubmitCommand]], missionTracker: MissionTracker) {

  // A behaviour[int] that is always zero
  val zero: Behaviour[Int] = new Constant(0)

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
  // Not separated by missoin
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


  // Result depends on previous mission's skill requirements and the totals behaviours
  val prevResults: Behaviour[Tuple2[MissionResult, MissionResult]] = missionTracker.prevMissions.map(evaluateResult(_))

  def evaluateResult(missions: Tuple2[Mission, Mission]): Tuple2[MissionResult, MissionResult] = {
    // TODO evaluate missions correctly
    // new MissionResult(mission, mission.primaryObjective.primary.min <= skillTotals[mission.primaryObjective.primary.skill].eval(), None, None)

    def sum(skills: List[Skill]): Int = {
      val submissionsList = for {
        submittedSkill <- skills
        camp <- submittedSkills.keys
        skill <- submittedSkills(camp).keys
        submitter <- submittedSkills(camp)(skill).keys
        if (submittedSkill == skill)
        } yield submittedSkills(camp)(skill)(submitter).eval
      submissionsList.reduce(_ + _)
      }
    def decompose(mission: Mission): MissionResult = {
      mission match {
        case Mission(camp, day, primaryObjective, secondaryObjective) => {
          val primary = primaryObjective match {
            case PrimaryObjective(skills, difficulty, andOrSingle, rewards) =>
              difficulty < (sum(primaryObjective.positiveSkills) - sum(primaryObjective.negativeSkills))
            }
          val secondary = secondaryObjective match {
            case SecondaryObjective(skills, difficulty, linked, opposed, rewards) => {
              if (linked && opposed) primary && (difficulty < (sum(secondaryObjective.positiveSkills) - sum(secondaryObjective.negativeSkills)))
              else if (linked && !opposed) primary && (difficulty < (sum(secondaryObjective.positiveSkills) - 6))
              else if (!linked && opposed) (difficulty < (sum(secondaryObjective.positiveSkills) - sum(secondaryObjective.negativeSkills)))
              else difficulty < (sum(secondaryObjective.positiveSkills) - 6)
              }
            }
            (primary, secondary)
          }
        }
      }

    (decompose(missions._1), decompose(missions._2))
  }


}

