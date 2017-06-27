package com.scanner.service.api.http

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import com.scanner.query.api.Wizzair
import com.scanner.service.api.Api.RequestParams
import com.scanner.service.api.http.CustomDirectives.{requestParams, tell}
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Iurii on 25-06-2017.
  */
class CustomDirectivesSpec extends WordSpec
                                   with Matchers
                                   with ScalatestRouteTest {



  "CustomDirectives" should {

    "tell a message to an actor and complete the request" in {

      val expectedResponse = "completed"

      val testActor = TestActorRef(new Actor(){
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
            complete(params.toString)
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
      ).toString

      Get(s"/test?origin=$origin&arrival=$arrival&from=$from&to=$to&airline=$airline&currency=$currency") ~> testRoute ~> check {
        responseAs[String] shouldBe expectedResult
      }

    }
  }

}
