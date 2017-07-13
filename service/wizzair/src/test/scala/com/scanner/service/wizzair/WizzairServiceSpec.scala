package com.scanner.service.wizzair

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.message.api.{GetConnectionsMessage, GetConnectionsResponse, GetFlightsResponse}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

/**
  * Created by Iurii on 13-06-2017.
  */
class WizzairServiceSpec extends TestKit(ActorSystem("testSystem"))
                                 with ImplicitSender
                                 with WordSpecLike
                                 with Matchers
                                 with BeforeAndAfterAll {
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val wizzairService = TestActorRef[WizzairService]

  "WizzairService" should  {
    "return all connections" in {
      wizzairService ! GetConnectionsMessage
      expectMsgPF(20 seconds) {
        case GetConnectionsResponse(connections) if connections.nonEmpty => true
      }
    }
  }

}
