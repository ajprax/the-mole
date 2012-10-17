package com.goldblastgames.themole

import scala.collection.mutable.Map

class Memoize[-T, +R](f: T => R) extends (T => R) {
  private[this] val cached = Map.empty[T, R]

  def apply(x: T): R = {
    if(cached.contains(x)) {
      cached(x)
    } else {
      val retVal = f(x)
      cached + (x -> retVal)
      retVal
    }
  }
}

object Memoize {
  def apply[T, R](f: T => R): T => R = new Memoize(f)
}
