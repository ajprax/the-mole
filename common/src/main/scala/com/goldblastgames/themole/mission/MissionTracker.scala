package com.goldblastgames.themole.mission

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Stepper

class MissionTracker(missionChange: TimedMissionChange) {

  // Behaviour valued at the current mission pair
  val currMissions: Behaviour[Tuple2[Mission, Mission]] =
    Stepper[Tuple2[Mission, Mission]]((null: Mission, null: Mission), missionChange)

  val nullTuple = (null: Mission, null: Mission)
  // Behaviour valued at the previous mission pair using the sliding tuple
  val prevMissions: Behaviour[Tuple2[Mission, Mission]] =
    missionChange
      .foldLeft((nullTuple, nullTuple))((prev: Tuple2[Tuple2[Mission, Mission], Tuple2[Mission, Mission]], curr: Tuple2[Mission, Mission]) => (prev._2, curr))
          .map(tup => tup._1)

/*
        (((null: Mission, null: Mission), (null: Mission, null: Mission)))
        ((prev: ((Mission, Mission), (Mission, Mission)), curr: (Mission, Mission))
        => (prev._2, curr))
          .map(tup => tup._1)
*/
  }

