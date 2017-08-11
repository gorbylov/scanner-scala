package com.scanner.service.currency.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.message.core.Message
import com.scanner.message.currency._
import com.scanner.service.core.actor.ActorService
import com.scanner.service.currency.ApilayerService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyService(apilayerService: ApilayerService) extends Actor
  with ActorLogging
  with ActorService {

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
    apilayerService.fetchCurrencies().onComplete{
      case Success(currencyResponse) =>
        state = currencyResponse.quotes.map{case (key, value) => key.substring(3) -> value}
      case Failure(error) =>
        log.error(s"An error occurred while fetching currencies state: $error")
    }
  }
}

object CurrencyService {
  def props(): Props = Props(new CurrencyService(new ApilayerService))
}

