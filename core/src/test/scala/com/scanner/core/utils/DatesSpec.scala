package com.scanner.core.utils

import java.time.LocalDate

import org.scalatest.{Matchers, WordSpec}

import com.scanner.core.utils.Dates._

/**
  * Created by IGorbylov on 25.05.2017.
  */
class DatesSpec extends WordSpec with Matchers {

  "Dates utils" should {

    "generate correct interval between two days from one month" in {
      val intervalOneMonth = (LocalDate.now(), LocalDate.now()).toMonthsInterval
      intervalOneMonth.size shouldBe 1
      intervalOneMonth.head shouldBe LocalDate.now().withDayOfMonth(1)
    }

    "generate correct interval between two days from different months" in {
      val intervalOneMonth = (LocalDate.now(), LocalDate.now().plusMonths(2)).toMonthsInterval
      intervalOneMonth.size shouldBe 3
      intervalOneMonth shouldBe List(LocalDate.now().withDayOfMonth(1), LocalDate.now().plusMonths(1).withDayOfMonth(1), LocalDate.now().plusMonths(2).withDayOfMonth(1))
    }

    "generate correct interval between two days in case 'from' date is after 'to' date" in {
      val intervalOneMonth = (LocalDate.now(), LocalDate.now().minusMonths(1)).toMonthsInterval
      intervalOneMonth.size shouldBe 2
      intervalOneMonth shouldBe List(LocalDate.now().minusMonths(1).withDayOfMonth(1), LocalDate.now().withDayOfMonth(1))
    }

  }
}
