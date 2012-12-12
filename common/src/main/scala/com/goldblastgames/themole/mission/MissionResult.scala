package com.goldblastgames.themole.mission

case class MissionResult (
  result: Tuple2[Boolean, Boolean],
  debriefings: List[MissionDebriefing]
  )

