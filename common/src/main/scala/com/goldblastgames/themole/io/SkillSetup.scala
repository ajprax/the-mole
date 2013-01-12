package com.goldblastgames.themole.io

// Skill value changes
// setup information for the client.  never displayed directly to the user
case class SkillSetup(
  player: String,
  regenValue: Int,
  bonusValues: List[Int],
  currentValues: List[Int],
  maxValues: List[Int],
  submittedSkillUSA: String,
  submittedskillUSSR: String,
  submittedValueUSA: Int,
  submittedValueUSSR: Int
) extends Packet
