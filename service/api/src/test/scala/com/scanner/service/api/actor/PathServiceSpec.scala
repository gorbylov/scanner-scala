package com.scanner.service.api.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActors.EchoActor
import akka.testkit.{ImplicitSender, TestActor, TestActors, TestKit, TestProbe}
import com.scanner.message.api._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
/**
  * Created by igorbylov on 11.07.17.
  */
class PathServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)

  val flightsAgregatorProbe = TestProbe()
  val airlineService = system.actorOf(Props(classOf[EchoActor]), "airlineService")
  val airlineServices = List(Wizzair -> system.actorSelection("/user/airlineService")) // TODO mock actor selection behavior
  val pathService = system.actorOf(
    Props(classOf[PathService], flightsAgregatorProbe.ref, airlineServices),
    "testPathService"
  )

  "PathService actor" should {
    "build graph" in {
      pathService ! BuildGraphMessage
      pathService ! GraphIsEmptyMessage
      expectMsgPF(5 seconds) {
        case GraphIsEmptyResponse(isEmpty) => isEmpty shouldBe false
      }
    }

    "build path between two airports" in {
      pathService ! BuildPathMessage
      // TODO find out how to test it
    }
  }

}
