package com.github.oetzi.echo

import com.github.oetzi.echo.Control.devMode

trait EchoSpecification {
  // Enable construction/initialization of echo components outside of a setup method.
  devMode()
}
