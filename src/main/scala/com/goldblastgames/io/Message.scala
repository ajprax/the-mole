package com.goldblastgames.io

// Chat Messages
case class Message(
  sender: String,
  channel: String,
  body: String
) extends Packet
