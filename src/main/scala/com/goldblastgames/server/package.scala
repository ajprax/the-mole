package com.goldblastgames

import com.github.oetzi.echo.core.Event

package object server {
  type ServerModule[T,U] = Map[Player, Event[T]] => Map[Player, Event[U]]
}
