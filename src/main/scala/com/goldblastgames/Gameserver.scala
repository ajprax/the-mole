package com.goldblastgames

import com.github.oetzi.echo.EchoApp
import com.github.oetzi.echo.core.Behaviour
import com.github.oetzi.echo.io.Receiver
import com.github.oetzi.echo.io.Sender

import com.goldblastgames.Nation._

object Gameserver extends EchoApp {
  def setup(args: Array[String]) {
    // TODO(Issue-16): This should be in configuration somewhere.
    val players = Seq(
      Player(2552, 2562, 1L, "Player1", America, America),
      Player(2553, 2563, 2L, "Player2", USSR, USSR),
      Player(2554, 2564, 3L, "Player3", America, USSR),
      Player(2555, 2565, 4L, "Player4", USSR, America)
    )
    val effects = Seq[ChatEffect](
      ChatEffect.redact(Behaviour(_ => true), _ => true),
      ChatEffect.shuffle(Behaviour(_ => true), _ => true)
    )

    // TODO: Specify period for missions happening in configuration. 
    val missionSource = new MissionSource(10000)

    // Build the socket sources.
    val socketSources = players
        .map(_.sourcePort)
        .map(Receiver(_) { msg: String => Behaviour( _ => msg ) })

    socketSources.map(_.foreach(println(_)))

    val messageSources = socketSources
      .map(_.filter(_ matches Message.messageRegex).map((_, msg) => Message.deserialize(msg)))
    val playerSources = Map((players zip messageSources): _*)

    // Chat system
    val chatModule = ServerModule.chat(effects)
    val chatSources = chatModule(playerSources)

    // Add missions into outgoing events
    // Missions are not affected by chat effects
    val missionSources = MissionSource.addMissions(chatSources, missionSource)
    
    // Merge different streams.
    val sinkStreams = missionSources
    
    val sinks = sinkStreams.map {
      case (player, stream) => Sender("localhost", player.sinkPort, stream.map((_, msg) => msg.toString))
    }
  }
}
