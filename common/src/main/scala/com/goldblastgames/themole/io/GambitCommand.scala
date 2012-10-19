package com.goldblastgames.themole.io

case class GambitCommand(gambitName: String)

// TODO: finish
object GambitCommand {
  val gambitRegex = """Gambit\((.*)\)""" // this is probbly wrong
  val gambitMatcher = gambitRegex.r

  def deserialize(serialized: String): GambitCommand = {
    val gambitMatcher(name) = serialized

    GambitCommand(name)
  }
}
