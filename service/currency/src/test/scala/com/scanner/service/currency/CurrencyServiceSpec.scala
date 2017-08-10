package com.scanner.service.currency

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.message.currency.{ConvertCurrencyMessage, ConvertCurrencyResponse, UpdateCurrencyStateMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with CurrencyConfig {


  override def afterAll = TestKit.shutdownActorSystem(system)

  "CurrencyService actor" should {

    val actorRef = TestActorRef(new CurrencyService(system.scheduler, schedulerInterval seconds) {
      override def updateState(): Unit = this.state = Map("EUR" -> 2, "UAH" -> 3) // TODO try to rewrite the test
    })

    "receive converted value for correct currencies" in {
      actorRef ! ConvertCurrencyMessage("EUR", "UAH", 1)
      expectMsg(ConvertCurrencyResponse(Some(1.5)))
    }

    "receive empty value for incorrect currencies" in {
      actorRef ! ConvertCurrencyMessage("ZZZ", "FFF", 1)
      expectMsg(ConvertCurrencyResponse(None))
    }

    "update currency state" in {
      val stateBeforeUpdate = actorRef.underlyingActor.state
      actorRef ! UpdateCurrencyStateMessage
      val stateAfterUpdate = actorRef.underlyingActor.state
      (stateBeforeUpdate eq stateAfterUpdate) shouldBe false
    }

    "update currency state by scheduler" in {
      val stateBeforeUpdate = actorRef.underlyingActor.state
      Thread.sleep(schedulerInterval * 2000)
      val stateAfterUpdate = actorRef.underlyingActor.state
      (stateBeforeUpdate eq stateAfterUpdate) shouldBe false
      system.terminate()
    }
  }
}
