package com.goldblastgames.server

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import com.github.oetzi.echo.core.Stepper

// TODO: Make a channel object explicitly that is an Event that returns events.
//       May necessitate modifying echo with the CanBuild pattern
import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.mission._
import com.goldblastgames.Nation._
import com.goldblastgames.Player

case class ServerModule[T, U] (
  frp: Map[Player, Event[T]] => Map[Player, Event[U]]
) extends (Map[Player, Event[T]] => Map[Player, Event[U]]) {
  def apply(sources: Map[Player, Event[T]]) = frp(sources)
}

object ServerModule {
  def mission(): ServerModule[SubmitCommand, Message] = {
    
    val missionChange = new TimedMissionChange(10000) 
    val currentMission = Stepper[Mission](Mission.dummyMission, missionChange)

    // Build new mission notifications
    ServerModule(
      sources => {
        // TODO(issue-33): Resolve missions based on submitted skills.

        // When the mission changes, notify everyone!
        // TODO Mission Debriefing for previous mission, 
        // which will be different per team
        // as they have different level of detail.
         val newMissionSinks = sources.keys.map {
          case p @ Player(name, camp, allegiance) => {
            p -> missionChange.map((_, mission) => new Message("Mission Center", name, mission.description))
          }
        }

      // TODO merge prevMissionDebriefing with newMissionNotification
      Map(newMissionSinks.toSeq: _*)
    })
  }  

  def chat(effects: Seq[ChatEffect]): ServerModule[Message, Message] = {

    def channel(name: String, input: Event[Message]) = input.filter(_.channel == name)

    ServerModule(sources => {
      // Merge sources.
      val allSources     = sources.values.reduce(_ merge _)
      val ussrSources    = sources.filterKeys(_.camp == USSR).values.reduce(_ merge _)
      val americaSources = sources.filterKeys(_.camp == America).values.reduce(_ merge _)

      // Create channels.
      val allChannel     = channel("All", allSources)
      val playerChannels = sources.keys
          .map(player => player -> channel(player.name, allSources))
          .toMap
      val campChannels   = Map(
        USSR -> channel("USSR", ussrSources),
        America -> channel("America", americaSources)
      )

      // Build sets of player's sink components.
      val sinks = sources.keys.map {
        case p @ Player(name, nation, _) => {
          val appliedEffects = effects.filter(_.select(p))
          val channelSinks = allChannel merge campChannels(nation) merge playerChannels(p)

          channelSinks.foreach(msg => println("[%s %s]".format(name, msg)))

          // TODO: Use these timestamps.
          p -> channelSinks.map((_, msg) => appliedEffects.foldLeft(msg)((x, f) => f(x)))
        }
      }
      sinks.toMap
    })

  }
}

