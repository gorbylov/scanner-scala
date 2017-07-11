package com.scanner.service.api

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.message.api.BuildGraphMessage
import com.scanner.service.api.actor.ApiService
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Iurii on 14-06-2017.
  */
class ApiServiceSpec extends TestKit(ActorSystem("testSystem"))
                             with ImplicitSender
                             with WordSpecLike
                             with Matchers
                             with BeforeAndAfterAll {

  override def afterAll = TestKit.shutdownActorSystem(system)


  // TODO find out how to test/mock actor selection
  val apiService = TestActorRef[ApiService](Props(classOf[ApiService], system.actorSelection("")), "")

  "ApiService" should  {
    "build connections graph" in {
      apiService ! BuildGraphMessage
      //apiService.underlyingActor.graph.isEmpty() shouldBe false
    }
  }

}
