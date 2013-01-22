package com.goldblastgames.themole.server

import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashSet
import scala.collection.immutable.ListSet

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.DataInputStream
import java.io.DataOutputStream

import com.github.oetzi.echo.core.Behaviour
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

  val listener = Listener(port)

  // Players and corresponding WebSockets are kept track of here.
  // This is mutable!
  val socketPlayers = new HashMap[WebSocket, Player]()
  // val playerSockets = new HashMap[Player, Behaviour[Set[WebSocket]]]()
  val playerSockets: Map[Player, Behaviour[ListSet[WebSocket]]] =
    players.map (player =>
      {
        val filteredConnect = listener.filter({x =>
          val (socket, input) = x
          deserialize(input) match {
            case Connect(name) if name == player.name => {
              socketPlayers(socket) = player
              true
            }
            case _ => {
              false
            }
          }
        })
        val newPlayerSockets = filteredConnect.map ({ case (_ ,(socket, _)) => socket })
        val playerWebsockets = newPlayerSockets.foldLeft(new ListSet[WebSocket]()){(acc, sock) => acc + sock}
        (player, playerWebsockets)
      }
    ).toMap

  val incomingPackets: EventSource[(Player, Packet)] =
    new EventSource[(Player, Packet)] {
      listener.foreach {
        case (socket, input) => {
          println("received raw input: %s".format(input))
          val packet = deserialize(input)
          packet match {
            case Connect(playerName) => {}

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
    players.map({ player =>
      val playerPackets: Event[Packet] = incomingPackets.filter ({ myArg =>
        val (play, _) = myArg
        play == player
      }).map {
        case(_, (_, pack)) => pack
      }
      (player, playerPackets)
    }).toMap


  val moduleOutputs: Map[Player, Event[Packet]] = module(inputs)

  // Write outputs to server to the correct sockets
  // This is the part to debug
  playerSockets.foreach {playersocketpair => {
    val(player, sockets) = playersocketpair
    val outgoing: Event[Packet] = moduleOutputs(player)
    val sampled:Event[(Packet,ListSet[WebSocket])] = sockets.sample(outgoing)
    sampled.map({(t, packetSocketPair) => {
      val (packet, currSockets) = packetSocketPair
      println("sending %s to %s".format(packet, currSockets))
      currSockets.map(_.send(serialize(packet)))
    }
    })
  }}
}

object Session {
  def apply(port: Int, players: Seq[Player])(module: Map[Player, Event[Packet]] => Map[Player, Event[Packet]]) =
      new Session(port, players, module)
}
