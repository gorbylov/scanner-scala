package com.scanner.service.wizzair

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.query.api.{GetConnectionsQuery, GetConnectionsResponse, GetOneWayFlightsResponse}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * Created by Iurii on 13-06-2017.
  */
class WizzairServiceTest extends TestKit(ActorSystem("testSystem"))
                                 with ImplicitSender
                                 with WordSpecLike
                                 with Matchers
                                 with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val wizzairService = TestActorRef[WizzairService]

  "WizzairWorker" should  {
    "return all connections" in {
      wizzairService ! GetConnectionsQuery
      expectMsgPF(20 seconds) {
        case GetConnectionsResponse(connections) if connections.nonEmpty => true
      }
    }
  }

}
