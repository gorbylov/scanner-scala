package com.scanner.service.core.collections

import scala.concurrent.Future

/**
  * Created by IGorbylov on 09.03.2017.
  */
object Utils {

  implicit class TraversableOfFutures[A, C[B] <: TraversableOnce[B]](traversable: C[Future[A]]) {
    def futureSequence(): Future[C[A]] = Future.sequence(traversable)
  }
}
