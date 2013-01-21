package com.goldblastgames.themole.server

import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashSet
import scala.collection.mutable.Set

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.DataInputStream
import java.io.DataOutputStream

import com.github.oetzi.echo.core.Event
import com.github.oetzi.echo.core.EventSource
import unfiltered.netty.websockets.WebSocket

import com.goldblastgames.themole.Player
import com.goldblastgames.themole.io.Connect
import com.goldblastgames.themole.io.Packet
import com.goldblastgames.themole.io.PacketSerialization._

/**
 * Provides the input/output capabilities for a server session.
 *
 * module is the actual behaviour of the server
**/

class Session private(
  val port: Int,
  val players: Seq[Player],
  val module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]
) {

  // Players and corresponding WebSockets are kept track of here.
  // This is mutable!
  val socketPlayers = new HashMap[WebSocket, Player]()
  val playerSockets = new HashMap[Player, Set[WebSocket]]()

  val incomingPackets: EventSource[(Player, Packet)] =
    new EventSource[(Player, Packet)] {
      Listener(port).foreach {
        case (socket, input) => {
          val packet = deserialize(input)
          packet match {
            // If it's a connect message, add that player to our map
            case Connect(playerName) => {
              println("Connection string received from: %s".format(playerName))
              val withName = players.filter(_.name == playerName)
              if (withName.isEmpty) {
                println("No such player with name: %s".format(playerName))
              } else {
                // add player to our map assuming only one player with that name
                val player = withName(0)
                socketPlayers(socket) = player
                if (!playerSockets.contains(player)) {
                  playerSockets(player) = new LinkedHashSet[WebSocket]()
                }
                playerSockets(player) += socket
              }
            }

            // for all other messages, simply occur it if the player is already
            // in our map
            case p: Packet => {
              if (socketPlayers.contains(socket)) {
                occur(socketPlayers(socket), p)
              } else {
                println("received message from unknown socket")
              }
            }
          }
        }
      }
    }

  // Inputs to the server
  val inputs: Map[Player, Event[Packet]] =
    players.map(
      player => {
        val playerPackets: Event[Packet] = incomingPackets.map {
          case (t, (play, pack)) if (play == player) => pack
        }
        (player, playerPackets)
      }
    ).toMap


  val moduleOutputs: Map[Player, Event[Packet]] = module(inputs)

  // Write outputs to server to the correct sockets
  moduleOutputs.map {
    case (play, packets) => {
      packets.map((t, pack) => playerSockets(play).map(_.send(serialize(pack))))
    }
  }
}

object Session {
  def apply(port: Int, players: Seq[Player])(module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]) =
      new Session(port, players, module)
}
