package com.goldblastgames.themole.chat

import scala.util.Random

import com.github.oetzi.echo.core.Behaviour

import com.goldblastgames.themole.Player
import com.goldblastgames.themole.io.Message

case class ChatEffect(
  val effect: Message => Message,
  val enabled: Behaviour[Boolean],
  val select: Player => Boolean
) extends (Message => Message) {
  def apply(msg: Message): Message = 
    if (enabled.eval) 
      effect(msg) 
    else 
      msg
}

object ChatEffect {
  // These are specific chat effects that are instances of the case class ChatEffect above.  
  // The effect: Message=>Message parameters are the functions that are implemented in the second half of this object.
  def redact(enabled: Behaviour[Boolean], select: Player => Boolean) = ChatEffect(redactMessage, enabled, select)
  def anonymize(enabled: Behaviour[Boolean], select: Player => Boolean) = ChatEffect(anonymizeMessage, enabled, select)
  def shuffle(enabled: Behaviour[Boolean], select: Player => Boolean) = ChatEffect(shuffleMessage, enabled, select)

 //----------------------------------------------------------- 
 // The following implement the methods that transform messages.
 //----------------------------------------------------------- 

  // Anonymize the message (sender -> "anonymous")
  def anonymizeMessage(msg: Message): Message = {
    return new Message("anonymous", msg.channel, msg.body)
  }

  // Redact parts of the message randomly
  def redactMessage(msg: Message): Message = {
    // Character to represent redactions
    val redactChar = "\u2588"

    // Probability for redacting a word.
    val redactProb = 0.25

    val random = new Random((msg.sender + msg.body).hashCode)
    val redacted =
      msg.body.split("""\s+""") // Split on whitespace
        .map(word => 
            // If random double is within probability for redacting,
            // redact this word.
            if (random.nextDouble < redactProb)
              redactChar * word.length
            else
              word
        ).mkString(" ") // Put it back together into a string.
      // TODO(Issue 12) You lose tabs/other whitespace when this happens.

    return new Message(msg.sender, msg.channel, redacted)
  }

  // Shuffle the words of a message randomly
  def shuffleMessage(msg: Message) = {
    val splitMsg = msg
        .body
        .split("""\s+""")
        .toSeq
    val shuffled = Random.shuffle(splitMsg)
        .reduce(_ + " " + _)
    Message(msg.sender, msg.channel, shuffled)
  }
}
