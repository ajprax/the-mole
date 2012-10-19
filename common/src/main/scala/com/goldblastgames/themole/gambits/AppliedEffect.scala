package com.goldblastgames.themole.gambits

import com.goldblastgames.themole.Player

case class AppliedEffect[-I,+O](
  targets: List[Player],
  effect: Effect[I,O]
)
