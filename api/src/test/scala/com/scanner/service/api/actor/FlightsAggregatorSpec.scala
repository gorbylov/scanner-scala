package com.scanner.service.api.actor

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.scanner.protocol.api._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class FlightsAggregatorSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)

  val apiService = TestProbe()
  val flightAggregator = system.actorOf(FlightsAggregator.props(apiService.ref))

  "FlightsAggregator" should {
    "receive flights, aggregate them and send result to ApiService" in {

      val requestId = UUID.randomUUID().toString
      val iev = Airport("IEV", "Kiev", 0.0, 0.0)
      val bud = Airport("BUD", "Budapest", 0.0, 0.0)
      val mxp = Airport("MXP", "Milan", 0.0, 0.0)
      val ievToBud = FlightView("any", iev, bud, LocalDateTime.now(), LocalDateTime.now(), Wizzair, 100.00, "UAH")
      val budToMxp = FlightView("any", bud, mxp, LocalDateTime.now(), LocalDateTime.now(), Wizzair, 100.00, "UAH")

      flightAggregator ! AggregateFlights(requestId, 3, List(List(ievToBud, budToMxp)))
      flightAggregator ! AggregateFlights(requestId, 3, List(List(ievToBud, budToMxp)))
      flightAggregator ! AggregateFlights(requestId, 3, List(List(ievToBud, budToMxp)))
      apiService.expectMsgPF(2 seconds) {
        case RequestResponse(reqId, flights) =>
          flights.size shouldBe 3
        case _ =>
          fail()
      }
    }
  }

}
