package com.goldblastgames.io

case class DeadDrop(
  body: String
)

object DeadDrop {
  val deadDropRegex = """DeadDrop\((.*)\)"""
  val deadDropMatcher = deadDropRegex.r
  def deserialize(serialized: String): DeadDrop = {
    val deadDropMatcher(body) = serialized

    DeadDrop(body)
  }
}
