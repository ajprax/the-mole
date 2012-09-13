package com.goldblastgames.io

import com.goldblastgames.mission._
import com.goldblastgames.skills.Skills
import com.goldblastgames.skills.Skills._

// Command for submitting skills.
case class SubmitCommand(
  sender: String,
  skill: Skill,
  amount: Int
)

object SubmitCommand {
  // Matches a submit command
  val submitRegex = """SubmitCommand\((.*),(.*),(.*)\)"""
  val submitMatcher = submitRegex.r
  def deserialize(serialized: String): SubmitCommand = {
    val submitMatcher(sender, skill, amount) = serialized

    SubmitCommand(sender, Skills.withName(skill), amount.toInt)
  }
}
