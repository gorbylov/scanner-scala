package com.scanner.message.api

import com.scanner.message.core.Failure

/**
  * Created by igorbylov on 11.07.17.
  */
sealed trait ApiFailure extends Failure

case class FailureMessage(status: Int, message: String) extends ApiFailure
