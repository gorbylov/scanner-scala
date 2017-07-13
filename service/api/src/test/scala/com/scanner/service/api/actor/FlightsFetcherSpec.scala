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

  val airlineServiceSelection = mockActorSelection("airlineServiceSelection"){
    case GetFlightsMessage(stepIndex, stepsCount, origin, arrival, from, to, currency) => {
      self ! GetFlightsResponse(
        stepIndex,
        stepsCount,
        List(
          FlightView(
            "flightNumber",
            origin.iata,
            arrival.iata,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            Wizzair,
            100.00,
            currency
          )
        )
      )
    }
  }

  /*val airlineServices = List(Wizzair -> airlineServiceSelection)
  val flightsFetcher = system.actorOf(Props(classOf[FlightsFetcher], flightsAggregatorProbe.ref, airlineServices), "flightsFetcher")

  "FlightsFetcher actor" should {
    "fetch flights for specified path" in {
      val requestId = UUID.randomUUID().toString
      val iev = Airport("IEV", "Kiev", 0.0, 0.0)
      val bud = Airport("BUD", "Budapest", 0.0, 0.0)
      val path = List(iev -> bud)
      flightsFetcher ! FetchFlightsForPathMessage(requestId, path, LocalDate.now(), LocalDate.now(), Nil, "UAH", OneWay)

    }
  }*/

}
