package com.goldblastgames

import scala.util.Random

import java.io.File

import _root_.reactive.EventSource
import _root_.reactive.Observing

// Chat Messages
case class Message(
  sender: String,
  channel: String,
  body: String
) {
  override def toString(): String = {
    return "%s: %s: %s".format(channel, sender, body)
  }
}

