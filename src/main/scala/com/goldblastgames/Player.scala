package com.goldblastgames

import com.goldblastgames.Nation._

case class Player(
  //abilities: Seq[Ability],
  name: String,
  camp: Nation,
  allegiance: Nation
)
