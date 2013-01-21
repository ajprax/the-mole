package com.goldblastgames.themole.mission

import com.goldblastgames.themole.Nation._

case class MissionReward(
  camp: Nation,
  rewardType: String,
  value: Option[Int]
  //effect: Option[Effect],
  //effectDescription: Option[String]
  // require (effect.isEmpty == effectDescription.isEmpty)
) {

  override def toString = {
    if (!value.isEmpty)
      "+%d %s".format(value, rewardType)
    else
      ""
  }
}
