package com.goldblastgames.io

case class Connect(
  name: String
)

object Connect {
  val connectRegex = """Connect\((.*)\)"""
  val connectMatcher = connectRegex.r
  def deserialize(serialized: String): Connect = {
    val connectMatcher(name) = serialized

    Connect(name)
  }
}
