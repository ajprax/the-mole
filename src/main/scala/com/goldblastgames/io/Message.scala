package com.goldblastgames.io

// Chat Messages
case class Message(
  sender: String,
  channel: String,
  body: String
)
object Message {
  val messageRegex = """Message\((.*),(.*),(.*)\)"""
  val messageMatcher = messageRegex.r
  def deserialize(serialized: String): Message = {
    val messageMatcher(sender, channel, body) = serialized

    Message(sender, channel, body)
  }
}
