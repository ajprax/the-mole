package com.goldblastgames.themole

import shapeless.Field

import com.goldblastgames.themole.chat.ChatEffect

package object conf {
  object portField    extends Field[Int]
  object playersField extends Field[Seq[Player]]
  object effectsField extends Field[Seq[ChatEffect]]
}
