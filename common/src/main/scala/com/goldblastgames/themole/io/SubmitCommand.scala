package com.goldblastgames.themole.io

import com.goldblastgames.themole.mission._
import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.skills.Skills._

// Command for submitting skills.
case class SubmitCommand(
  sender: String,
  skill: Skill,
  amount: Int
) extends Packet
