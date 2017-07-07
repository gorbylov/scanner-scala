package com.scanner.service.api.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by igorbylov on 07.07.17.
  */
class AirportServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)

  val airportService = TestActorRef[AirportService]

  "AirportService" should  {
    "fetch all airports before it started" in {
      airportService.underlyingActor.airportsState.empty shouldBe false
    }
  }

}
