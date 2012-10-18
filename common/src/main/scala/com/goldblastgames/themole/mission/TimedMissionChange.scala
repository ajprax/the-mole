package com.goldblastgames.themole.mission

import java.util.Timer
import java.util.TimerTask

import com.github.oetzi.echo.core.EventSource


class TimedMissionChange(period: Long) extends EventSource[Mission] {
  val timer = new Timer()
    class MissionTimerTask() extends TimerTask {
      override def run(): Unit = {
        // TODO(Issue-36): Generate new mission here instead of dummy mission
        val mission = Mission.nextMission
        occur(mission)
      }
    }

    // Wait _period_ milliseconds before starting
    timer.schedule(new MissionTimerTask(), 0, period)
}

