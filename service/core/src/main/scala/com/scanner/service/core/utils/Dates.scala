package com.scanner.service.core.utils

import java.time.LocalDate

/**
  * Created by IGorbylov on 25.05.2017.
  */
object Dates {

  implicit class DatesInterval(tuple: (LocalDate, LocalDate)) {

    def toMonthsInterval(): List[LocalDate] = {
      def loop(from: LocalDate, to: LocalDate, acc: List[LocalDate]): List[LocalDate] = (from, to) match {
        case (f, t) if f.withDayOfMonth(1).isAfter(t.withDayOfMonth(1)) => acc
        case (f, t) => loop(f.plusMonths(1), t, f.withDayOfMonth(1) :: acc)
      }
      val (from, to) = tuple
      (if (from.isBefore(to)) loop(from, to, List()) else loop(to, from, List())).reverse
    }

  }



}
