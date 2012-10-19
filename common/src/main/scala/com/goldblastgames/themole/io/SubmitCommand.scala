package com.goldblastgames.themole.io

import com.goldblastgames.themole.mission._
import com.goldblastgames.themole.skills.Skills
import com.goldblastgames.themole.skills.Skills._
import com.goldblastgames.themole.Nation._

// Command for submitting skills.
case class SubmitCommand(
  sender: String,
  camp: Nation, // Camp whose mission to submit to
  skill: Skill,
  amount: Int
) extends Packet
