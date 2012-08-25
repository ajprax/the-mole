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
  // Character to represent redactions
  val redactChar = "█"

  // Redact every redactEvery words
  val redactEvery = 3

  // Anonymize the message (sender -> "anonymous")
  def anonymize: Message = {
    return new Message("anonymous", channel, body)
  }

  // Redact parts of the message randomly
  def redact: Message = {
    val redacted =
      body.split("""\s+""") // Split on whitespace
        .map(word => 
          Random.nextInt(redactEvery) match {
            // If random int equals 0, redact it.
            case 0 => redactChar * word.length
            case _ => word
          }
        ).mkString(" ") // Put it back together into a string.
      // TODO You lose tabs/other whitespace when this happens.

    return new Message(sender, channel, redacted)
  }

  override def toString(): String = {
    return "%s: %s: %s".format(channel, sender, body)
  }
}

