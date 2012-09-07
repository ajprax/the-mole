package com.goldblastgames

import java.util.Timer
import java.util.TimerTask

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

// TODO(issue-34): Move timer functionality to echo
class MissionSource(val period: Long = 86400000) // default one day
  extends EventSource[Message] 
{

  val logger = LoggerFactory.getLogger(this.getClass)

  // TODO Set the period in the setup phase
  val timer = new Timer() 
  class MissionTimerTask() extends TimerTask {
    override def run(): Unit = {
      val mission = MissionGenerator.nextMission
      logger.info("Sending mission\n%s".format(mission.toString))
      occur(new Message("Mission Center", "All", mission.description))
    }
  }

  // Wait _period_ milliseconds before starting
  timer.schedule(new MissionTimerTask(), period, period)
  logger.info("MissionSource set up.")
}

object MissionSource {
  // Add missions to outgoing events
  def addMissions(playerSinks: Map[Player, Event[Message]], missions: MissionSource): Map[Player, Event[Message]] = {
    val withMissions = playerSinks.keys.map {
      case p: Player => p -> (playerSinks(p) merge missions)
    }
    Map(withMissions.toSeq: _*)
  }
}

