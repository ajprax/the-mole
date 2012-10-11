package com.goldblastgames.io

import com.goldblastgames.mission._
import com.goldblastgames.skills.Skills
import com.goldblastgames.skills.Skills._

// Command for submitting skills.
case class SubmitCommand(
  sender: String,
  skill: Skill,
  amount: Int
) extends Packet
