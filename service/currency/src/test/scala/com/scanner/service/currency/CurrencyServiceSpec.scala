package com.scanner.service.currency

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.query.currency.{ConvertCurrencyQuery, ConvertCurrencyResponse, UpdateCurrencyStateQuery}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyServiceSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with CurrencyConfig {

  "CurrencyService actor" should {

    val actorRef = TestActorRef(new CurrencyService(system.scheduler, schedulerInterval seconds) {
      override def fetchCurrencies(): Map[String, BigDecimal] = Map("EUR" -> 2, "UAH" -> 3) // preventing api calling
    })

    "receive converted value for correct currencies" in {
      actorRef ! ConvertCurrencyQuery("EUR", "UAH", 1)
      expectMsg(ConvertCurrencyResponse(Some(1.5)))
    }

    "receive empty value for incorrect currencies" in {
      actorRef ! ConvertCurrencyQuery("ZZZ", "FFF", 1)
      expectMsg(ConvertCurrencyResponse(None))
    }

    "update currency state" in {
      val stateBeforeUpdate = actorRef.underlyingActor.state
      actorRef ! UpdateCurrencyStateQuery
      val stateAfterUpdate = actorRef.underlyingActor.state
      (stateBeforeUpdate eq stateAfterUpdate) shouldBe false
    }

    "update currency state by scheduler" in {
      val stateBeforeUpdate = actorRef.underlyingActor.state
      Thread.sleep(interval * 2000)
      val stateAfterUpdate = actorRef.underlyingActor.state
      (stateBeforeUpdate eq stateAfterUpdate) shouldBe false
      system.terminate()
    }
  }
}
