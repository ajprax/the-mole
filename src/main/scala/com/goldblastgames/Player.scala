package com.goldblastgames

import com.goldblastgames.Nation._

case class Player(
  sourcePort: Int,
  sinkPort: Int,
  identifier: Long,
  name: String,
  camp: Nation,
  allChannelegiance: Nation
)
