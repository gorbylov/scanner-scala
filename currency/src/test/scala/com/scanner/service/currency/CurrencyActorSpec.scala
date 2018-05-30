package com.scanner.service.currency

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.scanner.protocol.currency._
import com.scanner.service.currency.ApilayerService.CurrencyResponse
import com.scanner.service.currency.service.ApilayerService
import com.scanner.service.currency.service.actor.CurrencyActor
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyActorSpec extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockFactory
  with CurrencyConfig {

  override def afterAll = TestKit.shutdownActorSystem(system)

  implicit val timeout = 1 second

  "CurrencyService actor" should {

    val testState = Map[String, BigDecimal]("USDEUR" -> 2.0, "USDUAH" -> 3.0)
    val apilayerServiceMock = mock[ApilayerService]
    (apilayerServiceMock.fetchCurrencies _)
      .expects()
      .returns(Future(CurrencyResponse(testState)))

    val currencyService = TestActorRef(new CurrencyActor(apilayerServiceMock))

    "update currencies state" in {
      currencyService ! UpdateCurrencyStateMessage
      currencyService ! GetCurrencyStateMessage
      expectMsgPF(timeout) {
        case GetCurrencyStateResponse(actualState) =>
          actualState.get("EUR") shouldBe Some(2.0)
          actualState.get("UAH") shouldBe Some(3.0)
          actualState.get("WRONG_KEY") shouldBe None
      }
    }

    "receive converted value for correct currencies" in {
      currencyService ! ConvertCurrencyMessage("EUR", "UAH", 1)
      expectMsg(ConvertCurrencyResponse(Some(1.5)))
    }

    "receive empty value for incorrect currencies" in {
      currencyService ! ConvertCurrencyMessage("ZZZ", "FFF", 1)
      expectMsg(ConvertCurrencyResponse(None))
    }

  }
}
