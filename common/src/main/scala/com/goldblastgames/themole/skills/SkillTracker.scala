package com.goldblastgames.themole.skills

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Constant
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.Stepper

import com.goldblastgames.themole.io.SubmitCommand
import com.goldblastgames.themole.Player
import com.goldblastgames.themole.mission._
import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._

class SkillTracker(sources: Map[Player, Event[SubmitCommand]], missionTracker: MissionTracker) {

  // A behaviour[int] that is always zero
  val zero: Behaviour[Int] = new Constant(0)

  // Behaviours for each skill of each player
  val submittedSkills: Map[Skill, Map[Player, Behaviour[Int]]] =
    Skills.values.map {
      case skill => {
        skill -> {
          sources.map{
            case (player, event) => {
              val filteredSubmissionAmount: Event[Int] = event
                .filter(submit => submit.skill == skill)
                .map((t, submission) => submission.amount)

              player -> new Stepper[Int](0, filteredSubmissionAmount)
            }
          }
        }
      }
    }.toMap
  
  // Behaviours for totals of skills
  val skillTotals: Map[Skill, Behaviour[Int]] = 
    submittedSkills.map{
      case (skill, submissions) => {
        skill -> {
          submissions.values.foldLeft(zero)(_.map2(_)((x, y) => x + y))
        }
      }
    }

  // Result depends on previous mission's skill requirements and the totals behaviours
  val prevResult: Behaviour[MissionResult] = missionTracker.prevMission.map(evaluateResult(_))

  def evaluateResult(mission: Mission): MissionResult = {
    // TODO evaluate missions correctly
    // new MissionResult(mission, mission.primaryObjective.primary.min <= skillTotals[mission.primaryObjective.primary.skill].eval(), None, None)
    new MissionResult(mission, true, None, None, skillTotals.map(x => x._1.toString + x._2.eval.toString).reduce(_ + " " +  _))
  }
}
