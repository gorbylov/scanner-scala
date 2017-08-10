package com.scanner.service.currency

import akka.actor.{Actor, ActorLogging, Scheduler}
import com.scanner.message.core.Message
import com.scanner.message.currency._
import com.scanner.service.core.actor.ActorService
import com.scanner.service.currency.CurrencyService.CurrencyResponse
import io.circe.generic.auto._
import io.circe.parser._


import scala.io.Source
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyService(scheduler: Scheduler, interval: FiniteDuration) extends Actor
  with ActorLogging
  with ActorService {

  scheduler.schedule(0 seconds, interval, self, UpdateCurrencyStateMessage)

  var state: Map[String, BigDecimal] = Map.empty

  override def handleMessage: Function[Message, Unit] = {

    case GetCurrenciesCoefficientMessage(from, to) =>
      val coefficient = calculateCoefficient(from, to)
      sender ! GetCurrenciesCoefficientResponse(coefficient)

    case ConvertCurrencyMessage(from, to, value) =>
      val convertedCurrencyValue = convertCurrency(from, to, value)
      sender ! ConvertCurrencyResponse(convertedCurrencyValue)

    case UpdateCurrencyStateMessage =>
      updateState()
  }

  def convertCurrency(from: String, to: String, value: BigDecimal): Option[BigDecimal] = {
    for {
      fromCoef <- state.get(from)
      toCoef <- state.get(to)
    } yield value * (toCoef / fromCoef)
  }

  def calculateCoefficient(from: String, to: String): Option[BigDecimal] = convertCurrency(from, to, 1)

  def updateState(): Unit = {
    val futureCurrencyResponse = for {
      content <- Future(Source.fromURL(CurrencyService.apiUrl, "UTF-8").mkString)
      json <- Future.fromTry(parse(content).toTry)
      currencyResponse <- Future.fromTry(json.as[CurrencyResponse].toTry)
    } yield currencyResponse

    futureCurrencyResponse.onComplete{
      case Success(currencyResponse) =>
        state = currencyResponse.quotes.map{case (key, value) => key.substring(3) -> value}
      case Failure(error) =>
        log.error(s"An error occurred while fetching currencies state: $error")
    }
  }

}

object CurrencyService {
  val accessKey = "304ab1caae3a0cff5303cab9619d6140&format=1"
  val apiUrl = s"http://www.apilayer.net/api/live?access_key=$accessKey"

  case class CurrencyResponse(quotes: Map[String, BigDecimal])
}

