package com.scanner.service.api.actor

import java.time.LocalDate
import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit, TestProbe}
import com.scanner.message.api._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
/**
  * Created by igorbylov on 07.07.17.
  */
class AirportServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)

  val pathServiceProbe = TestProbe()
  val airportService: ActorRef = system.actorOf(
    Props(classOf[AirportService], pathServiceProbe.ref),
    "testAirportService"
  )

  "AirportService actor" should  {
    "fetch all airports, save result to state and send it to PathService actor" in {
      airportService ! FetchAirportsMessage
      pathServiceProbe.expectMsgPF(5 seconds) {
        case BuildGraphMessage(state) =>
          state.nonEmpty shouldBe true
      }

      airportService ! GetAirportsStateMessage
      expectMsgPF(5 seconds) {
        case GetAirportsStateResponse(state) =>
          state.nonEmpty shouldBe true
      }
    }

    "gets airports by iata and forward message to PathService actor" in {
      airportService ! ResolveAirportMessage(
        UUID.randomUUID().toString,
        RequestParams("IEV", "BUD", LocalDate.now(), LocalDate.now(), Nil, "UAH", OneWay)
      )
      pathServiceProbe.expectMsgPF(5 seconds) {
        case BuildPathMessage(requestId, origin, arrival, params) =>
          origin.iata shouldBe "IEV"
          arrival.iata shouldBe "BUD"
      }
    }
  }

}
