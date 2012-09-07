package com.goldblastgames.io

import com.goldblastgames.mission._

case class SubmitCommand(
  sender: String,
  skill: Skills.Skill,
  amount: Int
)

object SubmitCommand {
  val submitRegex = """Submit\((.*),(.*),(.*)\)"""
  val submitMatcher = submitRegex.r
  def deserialize(serialized: String): SubmitCommand = {
    val submitMatcher(sender, skill, amount) = serialized

    SubmitCommand(sender, Skills.withName(skill), amount.toInt)
  }
}
