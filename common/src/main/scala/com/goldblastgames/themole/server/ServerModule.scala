package com.goldblastgames.themole.server

import java.util.Timer
import java.util.TimerTask

import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource

// TODO: Make a channel object explicitly that is an Event that returns events.
//       May necessitate modifying echo with the CanBuild pattern
import com.goldblastgames.themole.chat.ChatEffect
import com.goldblastgames.themole.gambits.AppliedEffect
import com.goldblastgames.themole.io.DeadDrop
import com.goldblastgames.themole.io.GambitCommand
import com.goldblastgames.themole.io.Message
import com.goldblastgames.themole.io.SubmitCommand
import com.goldblastgames.themole.mission._
import com.goldblastgames.themole.skills._
import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.Player

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

        missionSinks.toMap
      }
    )
  }

  def deadDrop(period: Long): ServerModule[DeadDrop, DeadDrop] = {
    ServerModule(
      sources => {
        // Timer for dead-drop collection.
        val sendDrops = new EventSource[Option[DeadDrop]] {
          new Timer().schedule(new TimerTask {
              override def run() {
                println("Sending DeadDrops")
                occur(None)
              }
          }, period, period)
        }

        sources.foreach { case (player, drop) => println("Received drop from %s: %s".format(player, drop)) }

        // TODO: Support dead-drop spoofing.
        // Filter out all dead-drops from non-mole players.
        val moleDrops: Map[Player, Event[DeadDrop]] = sources.filterKeys(player => player.camp != player.allegiance)

        // Get the moles/spies.
        val moles: Iterable[Player] = moleDrops.keys

        // Group the moles by their allegiance.
        val sides: Map[Nation, Iterable[Player]] = moles.groupBy(_.allegiance)

        // Get the players,
        sources.keys

            // pair them up with their allegiance's spies (notice the plural),
            .map(player => (player, sides(player.camp)))

            // transform this into a Map (the object, not the function),
            .toMap

            // do the following for every player's paired spies:
            .mapValues { spies =>

              // Get the dead-drops relevant to this player by doing the following:
              val drops: Event[Option[DeadDrop]] = spies

                  // Get the drops made by each spy,
                  .map(moleDrops(_))

                  // merge all of the drops into one event,
                  .reduce(_ merge _)

                  // convert each event into Some, the counterpart to None (as used in the 'sendDrops' event),
                  .map((_, drop) => Some(drop).asInstanceOf[Option[DeadDrop]])

                  // merge this stream with the 'sendDrops' event. This event stream makes use of
                  // the Option[T] object. This object is used to hold a value that may not exist.
                  // You can imagine the Option[T] class and its two subclasses: Some[T] and None[T]
                  // are defined as:
                  //
                  //   trait Option[T]
                  //   case class Some[T](val value: T) extends Option[T]
                  //   case class None[T] extends Option[T]
                  //
                  // With this setup you can do things like the following:
                  //
                  //   def printName(firstName: String, middleName: Option[String], lastName: String) {
                  //     middleName match {
                  //       case Some(name) => println("%s %s %s".format(firstName, name, lastName))
                  //       case None => println("%s %s".format(firstName, lastName))
                  //     }
                  //   }
                  //
                  //   ...
                  //
                  //   printName("Robert", Some("Michael"), "Chu")   // Prints: Robert Michael Chu
                  //   printName("Robert", None, "Chu")              // Prints: Robert Chu
                  //
                  // This is a very contrived example, but hopefully you get the idea.
                  .merge(sendDrops)

              // Fold the resulting dead-drop event producing a new event at the same time.
              new EventSource[DeadDrop] {

                // Use foldLeft's accumulator object to store the deaddrops awaiting collection.
                drops.foldLeft(Seq[DeadDrop]()) { (accumulator, event) =>

                  // Do something different for each option subclass.
                  event match {

                    // When a drop is received, add it to the accumulator (whatever the function
                    // passed to foldLeft's second argument group returns is the new accumulator).
                    case Some(drop) => accumulator ++ Seq(drop)

                    // When a None is received (from the 'sendDrops' event) have this event source
                    // fire an event for each dead-drop awaiting collection and then return an empty
                    // Seq[DeadDrop], clearing the accumulator.
                    case None => { accumulator.foreach(occur(_)); Seq() }
                  }
                }
              }
            }
      }
    )
  }

  def chat(effects: Behaviour[Seq[AppliedEffect[Message, Message]]]): ServerModule[Message, Message] = {

    def channel(name: String, input: Event[Message]) = input.filter(_.channel == name)
    ServerModule(
      sources => {
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
            val channelSinks = allChannel merge campChannels(nation) merge playerChannels(p)

            channelSinks.foreach(msg => println("[%s %s]".format(name, msg)))

            // TODO: Use these timestamps.
            p -> channelSinks.map((_, msg) => effects.eval.filter(_.targets.contains(p)).foldLeft(msg)((x, applied) => applied.effect(x)))
          }
        }
        sinks.toMap
      }
    )

  }

  // Gambit module to handle submission of gambit commands and responses.
  // Enables and disables, and sets targets of effects. (TODO)
  def gambit(): Map[Player, Event[GambitCommand]] => (Map[Player, Event[Message]], Behaviour[Seq[AppliedEffect[Any, Any]]]) = {
    sources: Map[Player, Event[GambitCommand]] => {
        "blope"
        val sinks: Map[Player, Event[Message]] =
          sources.map {
            case (player, commandEvent) => {
              val responseMessage: Event[Message] =
                  commandEvent.map((_, command) => new Message("blope", player.name, "Your gambit has been received."))
              (player, responseMessage)
            }
          }
        // This is the first half of the tuple:
        // TODO second half of the tuple for real
        (sinks, Behaviour[Seq[AppliedEffect[Any, Any]]](t => Seq()))
      }
  }
}

