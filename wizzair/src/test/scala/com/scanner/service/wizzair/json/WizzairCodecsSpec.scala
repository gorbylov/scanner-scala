package com.scanner.service.wizzair.json

import java.time.{LocalDate, LocalTime}

import org.scalatest.{Matchers, WordSpec}
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.service.wizzair.json.WizzairCodecs._

/**
  * Created by IGorbylov on 08.06.2017.
  */
class WizzairCodecsSpec extends WordSpec with Matchers {

  "WizzairCodecs" should {

    "successfully decode date" in {
      case class TestLocalDate(date: LocalDate)
      val json ="{ \"date\" : \"20170626\" }"
      parse(json).flatMap(_.as[TestLocalDate]).foreach{ testLocalDate =>
        testLocalDate shouldBe TestLocalDate(LocalDate.of(2017, 6, 26))
      }
    }

    "successfully decode time" in {
      case class TestLocalTime(date: LocalTime)
      val json ="{ \"date\" : \"07:51\" }"
      parse(json).flatMap(_.as[TestLocalTime]).foreach{ testLocalTime =>
        testLocalTime shouldBe TestLocalTime(LocalTime.of(7, 51))
      }
    }

  }

}
