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
    // TODO In the setup/initialization phase, start the timer
    // Instead of having a 10 second delay
    timer.schedule(new MissionTimerTask(), 1000L * 10, period)
}

