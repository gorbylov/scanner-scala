package com.scanner.service.api

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import com.scanner.query.api._
import com.scanner.service.api.marshal.ApiUnmarshallers._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import io.circe.generic.auto._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

/**
  * Created by IGorbylov on 09.03.2017.
  */
class ApiSpec extends WordSpec with Matchers with ScalatestRouteTest with Api {

  implicit val timeout = 5 seconds

  "The Scanner API" should {

    "return OK status for Get request to /scan path" in {

      val now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val nowPlusMonth = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      Get(s"/scan?origin=ORG&arrival=ARV&start=$now&end=$nowPlusMonth&airline=wizzair&currency=UAH") ~> routes(testApiService) ~> check {
        status shouldBe StatusCodes.OK
      }

      // TODO tests with wrong parameters
    }
  }

  val testApiService = TestActorRef(new Actor() {
    override def receive: Receive = {
      case oneWay: GetOneWayFlightsRequest => sender ! GetOneWayFlightsResponse(Seq.empty)
    }
  })

}
