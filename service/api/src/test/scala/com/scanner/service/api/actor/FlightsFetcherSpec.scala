package com.scanner.service.api.actor

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import akka.testkit.TestActors.EchoActor
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import com.scanner.message.api._
import com.scanner.service.core.actor.CustomActorMocking
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by igorbylov on 13.07.17.
  */
class FlightsFetcherSpec extends TestKit(ActorSystem("testSystem"))
  with CustomActorMocking
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val flightsAggregatorProbe = TestProbe()
  val (airlineServiceSelection, airlineServiceProbe) = mockActorSelection("airline-service")
  val airlineServices = List(Wizzair -> airlineServiceSelection)
  val requestId = UUID.randomUUID().toString
  val flightsFetcher = system.actorOf(
    Props(new FlightsFetcherPerRequest(requestId, flightsAggregatorProbe.ref, airlineServices)),
    "flightsFetcher"
  )

  "FlightsFetcher actor" should {

    "send message to airline service to get flights for specified parameters" in {
      val requestId = UUID.randomUUID().toString
      val iev = Airport("IEV", "Kiev", 0.0, 0.0)
      val bud = Airport("BUD", "Budapest", 0.0, 0.0)
      val path = List(iev -> bud)
      val from = LocalDate.now()
      val to = LocalDate.now()
      val uah = "UAH"

      flightsFetcher ! FetchFlightsForPathMessage(path, from, to, Nil, uah, OneWay)
      airlineServiceProbe.expectMsgPF(2 seconds) {
        case GetFlightsMessage(stepIndex, stepsCount, origin, arrival, fromDate, toDate, currency) =>
          stepIndex shouldBe 0
          stepsCount shouldBe path.size
          origin shouldBe iev
          arrival shouldBe bud
          from shouldBe fromDate
          to shouldBe toDate
          currency shouldBe uah
        case _ => fail
      }
    }

    "receive 3 responses from airline services, collect data and send to FlightsAggregator" in {
      val iev = Airport("IEV", "Kiev", 0.0, 0.0)
      val bud = Airport("BUD", "Budapest", 0.0, 0.0)
      val from = LocalDateTime.now()
      val to = LocalDateTime.now()
      val price = 100.00
      val uah = "UAH"
      val view = FlightView("flightNumber", iev, bud, from, to, Wizzair, price, uah)
      flightsFetcher ! GetFlightsResponse(0, 3, List(view))
      flightsFetcher ! GetFlightsResponse(1, 3, List(view))
      flightsFetcher ! GetFlightsResponse(2, 3, List(view))
      flightsFetcher ! GetFlightsStateMessage
      flightsAggregatorProbe.expectMsgPF(2 seconds) {
        case AggregateFlights(actualRequestId, flights) =>
          actualRequestId shouldBe requestId
          flights.size shouldBe 3
        case _ => fail()
      }
    }
  }

}
