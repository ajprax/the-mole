package com.goldblastgames

import _root_.reactive.Signal

case class ChatEffect(
  val effect: Message => Message,
  val enabled: Signal[Boolean],
  val select: Player => Boolean
) extends (Message => Message) {

  def apply(msg: Message): Message = 
    if (enabled.now) 
      effect(msg) 
    else 
      msg
}

object ChatEffect {
  def redact(enabled: Signal[Boolean], select: Player => Boolean) = ChatEffect(msg => msg.redact, enabled, select)
  def anonymize(enabled: Signal[Boolean], select: Player => Boolean) = ChatEffect(msg => msg.anonymize, enabled, select)
}


