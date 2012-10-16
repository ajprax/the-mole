package com.goldblastgames.gambits

import com.goldblastgames.Player

case class AppliedEffect[-I,+O](
  targets: List[Player],
  effect: Effect[I,O]
)
