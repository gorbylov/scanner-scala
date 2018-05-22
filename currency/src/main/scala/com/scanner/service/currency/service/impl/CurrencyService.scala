package com.scanner.service.currency.service.impl

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import akka.pattern.pipe
import akka.stream.Materializer
import com.scanner.protocol.currency._
import com.scanner.service.currency.CurrencyConfig
import com.scanner.service.currency.model.CurrencyState
import com.scanner.service.currency.protocol.{CurrenciesFetched, CurrenciesFetchingFailed, FetchCurrencies, CurrencyInitializationMessage}
import com.scanner.service.currency.service.CurrencyFetcher

import scala.concurrent.ExecutionContext

class CurrencyService(
  currencyFetcher: CurrencyFetcher
)(implicit ec: ExecutionContext, as: ActorSystem) extends Actor
  with Stash
  with ActorLogging
  with CurrencyConfig {

  var state: CurrencyState = CurrencyState.empty

  override def preStart() = self ! FetchCurrencies

  override def receive: Receive = fetchingBehaviour

  def fetchingBehaviour: Receive = {
    case FetchCurrencies =>
      log.info("Fetching currencies...")
      currencyFetcher.fetch()
        .map[CurrencyInitializationMessage](currencies => CurrenciesFetched(currencies.data))
        .recover { case ex => CurrenciesFetchingFailed(ex.getMessage)}
        .pipeTo(self)

    case CurrenciesFetched(data) =>
      log.info("Currencies successfully fetched.")
      state = CurrencyState(data)
      context become regularBehaviour
      as.scheduler.scheduleOnce(fetchingInterval, self, UpdateCurrencyStateMessage)
      unstashAll()

    case CurrenciesFetchingFailed(message) =>
      log.error(s"An error occurred during fetching currencies: $message. Next try in ${failedFetchingInterval.toSeconds} seconds.")
      as.scheduler.scheduleOnce(failedFetchingInterval, self, FetchCurrencies)

    case msg =>
      if (state.nonEmpty && state.notExpired) regularBehaviour(msg)
      else stash()
  }

  def regularBehaviour: Receive = {
    case GetCurrenciesCoefficientMessage(from, to) =>
      val coefficient = calculateCoefficient(from, to)
      sender ! GetCurrenciesCoefficientResponse(coefficient)

    case ConvertCurrencyMessage(from, to, value) =>
      val convertedCurrencyValue = convertCurrency(from, to, value)
      sender ! ConvertCurrencyResponse(convertedCurrencyValue)

    case UpdateCurrencyStateMessage =>
      context become fetchingBehaviour
      self ! FetchCurrencies
  }

  def convertCurrency(from: String, to: String, value: BigDecimal): Option[BigDecimal] = {
    for {
      fromCoef  <- state.data.get(from)
      toCoef    <- state.data.get(to)
    } yield value * (toCoef / fromCoef)
  }

  def calculateCoefficient(from: String, to: String): Option[BigDecimal] = convertCurrency(from, to, 1)
}

object CurrencyService {
  def props()(implicit ec: ExecutionContext, as: ActorSystem, m: Materializer): Props = {
    Props(new CurrencyService(new ApilayerCurrencyFetcher()))
  }
}

