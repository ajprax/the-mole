package com.github.oetzi.echo

import com.github.oetzi.echo.core.EventSource

class TestEvent[T] extends EventSource[T] {
  def pubOccur(value: T) {
    occur(value)
  }
}
