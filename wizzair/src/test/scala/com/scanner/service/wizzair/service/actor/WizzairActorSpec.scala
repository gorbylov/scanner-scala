package com.scanner.service.wizzair.service.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.protocol.api.{GetConnectionsMessage, GetConnectionsResponse}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class WizzairActorSpec extends TestKit(ActorSystem("testSystem"))
                                 with ImplicitSender
                                 with WordSpecLike
                                 with Matchers
                                 with BeforeAndAfterAll {
  override def afterAll = TestKit.shutdownActorSystem(system)

  val wizzairService = TestActorRef[WizzairActor]

  "WizzairService" should  {
    "return all connections" in {
      wizzairService ! GetConnectionsMessage
      expectMsgPF(20 seconds) {
        case GetConnectionsResponse(connections) if connections.nonEmpty => true
      }
    }
  }

}