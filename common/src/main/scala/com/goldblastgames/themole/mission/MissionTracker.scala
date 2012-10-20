package com.goldblastgames.themole.mission

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Stepper

class MissionTracker(missionChange: TimedMissionChange) {

  // Behaviour valued at the current mission
  val currMission: Behaviour[Mission] = Stepper[Mission](null: Mission, missionChange)

  // Behaviour valued at the previous mission using the sliding tuple
  val prevMission: Behaviour[Mission] =
    missionChange
      .foldLeft((null: Mission, null: Mission))((prev: Tuple2[Mission, Mission], curr: Mission) =>  (prev._2, curr))
      .map(tup => tup._1)
}

