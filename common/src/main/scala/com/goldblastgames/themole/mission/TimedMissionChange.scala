package com.goldblastgames.themole.mission

import java.util.Timer
import java.util.TimerTask

import com.github.oetzi.echo.core.EventSource


class TimedMissionChange(period: Long) extends EventSource[Tuple2[Mission, Mission]] {
  val timer = new Timer()
    class MissionTimerTask() extends TimerTask {
      override def run(): Unit = {
        val missions = Mission.nextMissions
        println("missions occur " + missions)
        occur(missions)
      }
    }

    // Wait _period_ milliseconds before starting
    // TODO In the setup/initialization phase, start the timer
    // Instead of having a 10 second delay
    timer.schedule(new MissionTimerTask(), 10000L, period)
    println("mission timer started")
}

