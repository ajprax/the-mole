package com.goldblastgames.themole.io

import scala.util.parsing.json._

import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.Nation

/**
 * JSON Serialization for our own Packet-extending case classes.
 *
 * If you extend Packet, you'll have to add the fields of your new class
 * and add cases to serialize and deserialize.
 *
 * If you change a Packet, you'll have to change the fields used below
 * and possibly the deserialize case for different types.
 **/
object PacketSerialization {

  // If you change the definition of a case class, change the fields here.
  val connectFields = List("name")
  val deadDropFields = List("body")
  val messageFields = List("sender","channel","body")
  val submitFields = List("sender","camp","skill","amount")
  val skillSetupFields = List("player","regenValue","bonusValues","currentValues","maxValues"
    ,"submittedSkillUSA","submittedSkillUSSR","submittedValueUSA","submittedValueUSSR")
  val playerInfoFields = List("player","skillBonuses","skillMaxes","gambits","secretive"
    ,"camp")
  val missionInfoFields = List("camp","day","skillOne","skillTwo","skillThree","rewardsOne"
    ,"rewardsTwo","rewardsInfoOne","rewardsInfoTwo","difficulty","primaryType","linked"
    ,"opposed")
  val errorMessageFields = List("title","body")

  def serialize(packet: Packet): String = {

    // Makes a JSONObject from a Packet including its type
    // and the specified, ordered names for its fields.
    // length of the fields must be the number of fields of the packet case class
    def jsonFromPacket(fields: List[String], packet: Packet): JSONObject = {
      assert(fields.size == packet.productIterator.size)
      return new JSONObject(
        {fields zip packet.productIterator.toList.map(_.toString)}.toMap + (("type", packet.productPrefix))
      )
    }

    packet match {
      case p: Connect => {
        return jsonFromPacket(connectFields, p).toString
      }
      case p: DeadDrop => {
        return jsonFromPacket(deadDropFields, p).toString
      }
      case p: Message => {
        return jsonFromPacket(messageFields, p).toString
      }
      case p: SubmitCommand => {
        return jsonFromPacket(submitFields, p).toString
      }
      case p: SkillSetup => {
        return jsonFromPacket(skillSetupFields, p).toString
      }
      case p: PlayerInfo => {
        return jsonFromPacket(playerInfoFields, p).toString
      }
      case p: MissionInfo => {
        return jsonFromPacket(missionInfoFields, p).toString
      }
      case p: ErrorMessage => {
        return jsonFromPacket(missionInfoFields, p).toString
      }
      case p => {
        throw new RuntimeException("Can't serialize Packet: %s".format(p))
      }
    }
  }

  def deserialize(json: String): Packet = {
    // Synchronized for now because the JSON object has mutable state.
    // TODO: If performance becomes an issue, try to use a thread-local variable
    // for the JSON parsing object.  Try to do it without copying the entire
    // class from the scala library.
    synchronized {
      val parsed = JSON.parseRaw(json)

      parsed match {
        case None => throw new RuntimeException("Can't unparse: %s".format(json))
        case Some(x) => {
          x match {
            case array: JSONArray => {
              throw new RuntimeException("Expecting a JSONObject not a JSONArray: %s".format(array))
            }
            case jsonObject: JSONObject => {
              val jsonFields = jsonObject.obj
              val packetType = jsonFields("type")

              // Match the type of message
              if (packetType == "Connect") {
                new Connect(jsonFields(connectFields(0)).asInstanceOf[String])
              } else if (packetType == "DeadDrop") {
                new DeadDrop(jsonFields(deadDropFields(0)).asInstanceOf[String])
              } else if (packetType == "Message") {
                // I really wish I could do something like this instead:
                // messageFields.map(x => obj(x).asInstanceOf[String]).foldLeft(Message.curried)((p,n) => p(n))
                new Message(
                  jsonFields(messageFields(0)).asInstanceOf[String],
                  jsonFields(messageFields(1)).asInstanceOf[String],
                  jsonFields(messageFields(2)).asInstanceOf[String]
                )
              } else if (packetType == "SubmitCommand") {
                new SubmitCommand(
                  jsonFields(submitFields(0)).asInstanceOf[String],
                  Nation.withName(jsonFields(submitFields(1)).asInstanceOf[String]),
                  Skills.withName(jsonFields(submitFields(2)).asInstanceOf[String]),
                  jsonFields(submitFields(3)).asInstanceOf[String].toInt
                )
              } else if (packetType == "SkillSetup") {
                // shouldn't ever have to deserialize this type because it is server
                // to client only, but safer to have the option
                new SkillSetup(
                  jsonFields(skillSetupFields(0)).asInstanceOf[String],
                  jsonFields(skillSetupFields(1)).asInstanceOf[Int],
                  jsonFields(skillSetupFields(2)).asInstanceOf[List[Int]],
                  jsonFields(skillSetupFields(3)).asInstanceOf[List[Int]],
                  jsonFields(skillSetupFields(4)).asInstanceOf[List[Int]],
                  jsonFields(skillSetupFields(5)).asInstanceOf[String],
                  jsonFields(skillSetupFields(6)).asInstanceOf[String],
                  jsonFields(skillSetupFields(7)).asInstanceOf[Int],
                  jsonFields(skillSetupFields(8)).asInstanceOf[Int]
                )
              } else if (packetType == "PlayerInfo") {
                // shouldn't ever have to deserialize this type because it is server
                // to client only, but safer to have the option
                new PlayerInfo(
                  jsonFields(playerInfoFields(0)).asInstanceOf[String],
                  jsonFields(playerInfoFields(1)).asInstanceOf[List[Int]],
                  jsonFields(playerInfoFields(2)).asInstanceOf[List[Int]],
                  jsonFields(playerInfoFields(3)).asInstanceOf[List[String]],
                  jsonFields(playerInfoFields(4)).asInstanceOf[Boolean],
                  jsonFields(playerInfoFields(5)).asInstanceOf[String]
                )
              } else if (packetType == "MissionInfo") {
                // shouldn't ever have to deserialize
                new MissionInfo(
                  Nation.withName(jsonFields(missionInfoFields(0)).asInstanceOf[String]),
                  jsonFields(missionInfoFields(1)).asInstanceOf[Int],
                  Skills.withName(jsonFields(missionInfoFields(2)).asInstanceOf[String]),
                  Skills.withName(jsonFields(missionInfoFields(3)).asInstanceOf[String]),
                  Skills.withName(jsonFields(missionInfoFields(4)).asInstanceOf[String]),
                  jsonFields(missionInfoFields(5)).asInstanceOf[String],
                  jsonFields(missionInfoFields(6)).asInstanceOf[String],
                  jsonFields(missionInfoFields(7)).asInstanceOf[String],
                  jsonFields(missionInfoFields(8)).asInstanceOf[String],
                  jsonFields(missionInfoFields(9)).asInstanceOf[Int],
                  jsonFields(missionInfoFields(10)).asInstanceOf[String],
                  jsonFields(missionInfoFields(11)).asInstanceOf[Boolean],
                  jsonFields(missionInfoFields(12)).asInstanceOf[Boolean]
                )
              } else if (packetType == "ErrorMessage") {
                // shouldn't ever have to deserialize
                new ErrorMessage(
                  jsonFields(errorMessageFields(0)).asInstanceOf[String],
                  jsonFields(errorMessageFields(1)).asInstanceOf[String]
                )
              } else {
                throw new RuntimeException(
                  "Cannot deserialize unknown packet type: %s".format(jsonObject)
                )
              }
            }
          }
        }
      }
    }
  }
}

