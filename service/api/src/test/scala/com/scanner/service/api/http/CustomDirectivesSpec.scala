package com.scanner.service.api.http

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import com.scanner.query.api.Wizzair
import com.scanner.service.api.Api.{FailureMessage, RequestParams}
import com.scanner.service.api.http.CustomDirectives.{requestParams, tell, validate}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.scalatest.{Matchers, WordSpec}

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import com.scanner.service.core.json.BasicCodecs._

/**
  * Created by Iurii on 25-06-2017.
  */
class CustomDirectivesSpec extends WordSpec
                                   with Matchers
                                   with ScalatestRouteTest
                                   with CirceSupport {



  "CustomDirectives" should {

    "tell a message to an actor and complete the request" in {

      val expectedResponse = "completed"

      val testActor = TestActorRef(new Actor() {
        override def receive: Receive = {
          case ctx: ImperativeRequestContext => ctx.complete(expectedResponse)
        }
      })

      val testRoute = path("test") {
        get {
          tell { ctx =>
            testActor ! ctx
          }
        }
      }

      Get("/test") ~> testRoute ~> check {
        responseAs[String] shouldBe "completed"
      }
    }


    "fetch a request param object" in {
      val testRoute = path("test") {
        get {
          requestParams { params =>
            complete(params.asJson.toString())
          }
        }
      }

      val origin = "IEV"
      val arrival = "BUD"
      val from = "2017-01-01"
      val to = "2017-12-31"
      val airline = "wizzair"
      val currency = "UAH"
      val expectedResult = RequestParams(
        origin,
        arrival,
        LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        Wizzair :: Nil,
        currency
      )

      Get(s"/test?origin=$origin&arrival=$arrival&from=$from&to=$to&airline=$airline&currency=$currency") ~> testRoute ~> check {
        status shouldBe OK
        val response = responseAs[String]
        parse(response).flatMap(_.as[RequestParams]).fold(
          error => fail(error),
          actualResult => actualResult shouldBe expectedResult
        )
      }
    }

    "validate request param object" in {

      val successMessage = "success"

      val testRoute = path("test") {
        get {
          requestParams { params =>
            validate(params) {
              complete(successMessage)
            }
          }
        }
      }

      val badOrigin = "AAAA"
      val badArrival = "BBBB"
      val badFrom = "2017-01-01"
      val badTo = "2016-01-01"
      val airline = "wizzair"
      val badCurrency = "CCCC"

      Get(s"/test?origin=$badOrigin&arrival=$badArrival&from=$badFrom&to=$badTo&airline=$airline&currency=$badCurrency") ~> testRoute ~> check {
        status shouldBe BadRequest
        val response = responseAs[FailureMessage]
        response.status shouldBe 400
        response.message.split('.').length shouldBe 5
      }

      val origin = "AAA"
      val arrival = "BBB"
      val from = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val to = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val currency = "CCC"

      Get(s"/test?origin=$origin&arrival=$arrival&from=$from&to=$to&airline=$airline&currency=$currency") ~> testRoute ~> check {
        status shouldBe OK
        responseAs[String] shouldBe successMessage
      }

    }
  }
}
