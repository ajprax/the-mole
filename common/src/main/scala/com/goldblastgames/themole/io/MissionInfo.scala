package com.goldblastgames.themole.io

import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._

// Mission information for the mission page
case class MissionInfo(
  camp: Nation,
  day: Int,
  skillOne: Skill, // first skill for primary objective
  skillTwo: Skill, // optional second skill for primary objective
  skillThree: Skill, // skill for secondary objective
  rewardsOne: String, // primary reward
  rewardsTwo: String, // secondary reward
  rewardsInfoOne: String, // hidden info for primary reward
  rewardsInfoTwo: String, // hidden info for secondary reward
  difficulty: Int,
  primaryType: String, // and, or, single
  linked: Boolean,
  opposed: Boolean
) extends Packet
