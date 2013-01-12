package com.goldblastgames.themole

import com.goldblastgames.themole.Nation._
import com.goldblastgames.themole.skills.Skills._

case class Player(
  //abilities: Seq[Ability],
  name: String,
  camp: Nation,
  allegiance: Nation,
  skillsRanges: Map[Skill, (Int, Int)] // (min, max)
)
