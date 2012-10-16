package com.goldblastgames.gambits

import com.goldblastgames.Player

class AppliedEffect[+I,+O](
  targets: List[Player],
  effect: Effect[I,O]
)
