package com.scanner.core.utils

import java.io.{PrintWriter, StringWriter}

/**
  * Created by Iurii on 12-06-2017.
  */
object Exceptions {

  implicit class ExceptionUtils(e: Throwable) {
    def mkString(): String = {
      val sw = new StringWriter
      val pw = new PrintWriter(sw)
      e.printStackTrace(pw)
      val result = sw.toString
      sw.close()
      pw.close()
      result
    }
  }

}
