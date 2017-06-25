package com.scanner.service.api.directive

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec, WordSpecLike}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RequestContext
import com.scanner.service.api.directive.CustomDirectives.{ImperativeRequestContext, tell}
/**
  * Created by Iurii on 25-06-2017.
  */
class CustomDirectivesSpec extends WordSpec
                                   with Matchers
                                   with ScalatestRouteTest {

  "CustomDirectives" should {

    "tell a message to actor and complete the request" in {
      val expectedResponse = "completed"
      val testActor = TestActorRef(new Actor(){
        override def receive: Receive = {
          case ctx: ImperativeRequestContext => ctx.complete(expectedResponse)
        }
      })
      val testRoute = path("test") {
        get {
          tell { ctx =>
            testActor ! ctx
          }
        }
      }
      Get("/test") ~> testRoute ~> check {
        responseAs[String] shouldBe "completed"
      }
    }
  }

}
