package com.scanner.service.wizzair.service.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by IGorbylov on 08.06.2017.
  */
class WizzairWorkerSpec extends TestKit(ActorSystem("testSystem"))
                                with ImplicitSender
                                with WordSpecLike
                                with Matchers
                                with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)

  val wizzairWorker = TestActorRef[WizzairActorWorker]

  "WizzairWorker" should  {
    "return list of flights for specified direction and dates" in {
      /*wizzairWorker ! GetFlightsMessage("IEV", "BUD", LocalDate.now(), LocalDate.now().plusMonths(1), Seq(), "UAH")
      expectMsgPF(5 seconds) {
        case GetOneWayFlightsResponse(flights) if flights.nonEmpty => true
      }*/

    }
  }
}
