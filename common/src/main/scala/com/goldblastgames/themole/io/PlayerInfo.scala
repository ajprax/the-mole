package com.goldblastgames.themole.io

// Player information for Dossier
case class PlayerInfo(
  player: String,
  skillBonuses: List[Int],
  skillMaxes: List[Int],
  gambits: List[String],
  secretive: Boolean,
  camp: String
) extends Packet
