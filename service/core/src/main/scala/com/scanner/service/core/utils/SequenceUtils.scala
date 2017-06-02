package com.scanner.service.core.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by Iurii on 02-06-2017.
  */
object SequenceUtils {

  implicit class TrySequence[T](list: List[Try[T]]) {
    def sequence(): Try[List[T]] = {
      def loop(in: List[Try[T]], acc: Try[List[T]]): Try[List[T]] = in match {
        case Nil => acc.map(_.reverse)
        case Failure(e) :: _ => Failure(e)
        case Success(el) :: tail => loop(tail, acc.map(el :: _))
      }
      loop(list, Try(Nil))
    }
  }

  implicit class FutureSequence[T](list: List[Future[T]]) {
    def sequence(implicit ec: ExecutionContext): Future[List[T]] = Future.sequence(list)
  }
}