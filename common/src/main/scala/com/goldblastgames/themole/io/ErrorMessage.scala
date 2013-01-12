package com.goldblastgames.themole.io

// inform the user if something goes wrong (e.g. invalid skill submission)
case class ErrorMessage(
  title: String,
  body: String
) extends Packet
