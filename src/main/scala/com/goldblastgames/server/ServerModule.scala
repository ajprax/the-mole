package com.goldblastgames.server

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

// TODO: Make a channel object explicitly that is an Event that returns events.
//       May necessitate modifying echo with the CanBuild pattern
import com.goldblastgames.chat.ChatEffect
import com.goldblastgames.io.Message
import com.goldblastgames.io.SubmitCommand
import com.goldblastgames.mission._
import com.goldblastgames.skills._
import com.goldblastgames.Nation._
import com.goldblastgames.Player

case class ServerModule[T, U] (
  frp: Map[Player, Event[T]] => Map[Player, Event[U]]
) extends (Map[Player, Event[T]] => Map[Player, Event[U]]) {
  def apply(sources: Map[Player, Event[T]]) = frp(sources)
}

object ServerModule {
  def mission(missionSource: TimedMissionChange): ServerModule[SubmitCommand, Message] = {
        
    ServerModule(
      sources => {
        // Mission Tracker
        val missionTracker = new MissionTracker(missionSource)

        // TODO(issue-33): Resolve missions based on submitted skills.

        //TODO reset skilltracker after each mission
        val skillTracker = new SkillTracker(sources, missionTracker)

        val missionSinks = sources.keys.map {
          case p @ Player(name, camp, allegiance) => {
            p -> {
              // Previous mission debriefing
              val debriefing: Event[Message] = 
                missionSource.map((_, mission) => 
                  new Message(
                    "Mission Report", 
                    name, 
                    skillTracker.prevResult.eval().toString
                  )
                )

              // New mission notifications
              val notifications: Event[Message] = missionSource.map((_, mission) => new Message("Mission Center", name, mission.toString))

              // Merge new mission notifications and debriefing notifications
              debriefing merge notifications
            }
          }
        }

      Map(missionSinks.toSeq: _*)
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

