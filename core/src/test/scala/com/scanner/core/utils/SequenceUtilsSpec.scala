package com.scanner.core.utils

import com.scanner.core.utils.SequenceUtils.{FutureSequence, TrySequence}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by Iurii on 02-06-2017.
  */
class SequenceUtilsSpec extends WordSpec with Matchers {

  "TrySequence" should {
    "correctly convert list of trys to try of list" in {
      val exception = new NullPointerException
      List(Try(1)).sequence() shouldBe Success(List(1))
      List(Try(1), Try(2), Try(3)).sequence() shouldBe Success(List(1, 2, 3))
      List(Try(throw exception)).sequence() shouldBe  Failure(exception)
      List(Try(1), Try(2), Try(throw exception)).sequence() shouldBe Failure(exception)
    }
  }

  "FutureSequence" should {
    "correctly convert list of futures to future of list" in {
      val exception = new NullPointerException
      Await.result(List(Future(1)) sequence, 5 seconds) shouldBe List(1)
      Await.result(List(Future(1), Future(2), Future(3)) sequence, 5 seconds) shouldBe List(1, 2, 3)
      assertThrows[NullPointerException] {
        Await.result(List(Future(throw exception)) sequence, 5 seconds)
      }
      assertThrows[NullPointerException] {
        Await.result(List(Future(1), Future(2), Future(throw exception)) sequence, 5 seconds)
      }
    }
  }
}
