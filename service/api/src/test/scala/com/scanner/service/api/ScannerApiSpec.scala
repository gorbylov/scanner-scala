package com.scanner.service.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by IGorbylov on 09.03.2017.
  */
class ScannerApiSpec extends WordSpec with Matchers with ScalatestRouteTest with ScannerApi {

  "The Scanner API" should {

    "return Hello message for Get request to /scan path" in {

      Get("/scan?origin=origin&arrival=arrival&start=2017-01-01&end=2017-02-02&airline=wizzair&airline=rianair&currency=UAH") ~> route() ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "Hello"
      }
    }
  }

}
