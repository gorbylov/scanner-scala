package com.scanner.core.implicits

object Predef {

  implicit class RichardBoolean(val boolean: Boolean) extends AnyVal {
    def fold[T](yes: => T)(no: => T) = if (boolean) yes else no
  }

}
