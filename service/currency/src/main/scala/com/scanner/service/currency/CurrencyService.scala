package com.scanner.service.currency

import akka.actor.{Actor, ActorLogging, Scheduler}
import com.scanner.query.currency.{ConvertCurrencyQuery, ConvertCurrencyResponse, UpdateCurrencyStateQuery}
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory
import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

/**
  * Created by Iurii on 06-03-2017.
  */
class CurrencyService(scheduler: Scheduler, interval: FiniteDuration) extends Actor with ActorLogging {

  scheduler.schedule(interval, interval, self, UpdateCurrencyStateQuery)

  var state: Map[String, BigDecimal] = fetchCurrencies()

  override def receive: Receive = {
    case ConvertCurrencyQuery(from, to, value) => sender ! ConvertCurrencyResponse(convert(from, to, value))
    case UpdateCurrencyStateQuery =>
      log.info("Updating currency state.")
      state = fetchCurrencies()
  }

  def convert(from: String, to: String, value: BigDecimal): Option[BigDecimal] = {
    for {
      fromCoef <- state.get(from)
      toCoef <- state.get(to)
    } yield value * (toCoef / fromCoef)
  }

  def fetchCurrencies(): Map[String, BigDecimal] = {
    Try(Source.fromURL(CurrencyService.API_URL).mkString)
      .flatMap(content => parse(content)
        .flatMap(json => json.as[CurrencyResponse])
        .fold(
          error => Failure(error),
          state => Success(state)
        )
      )
      .fold(
        error => {
          log.error(s"Error occurred while fetching currencies state: $error")
          Map[String, BigDecimal]()
        },
        state => state.quotes.map{case (key, value) => key.substring(3) -> value}
      )
  }
}

object CurrencyService {
  val API_URL = "http://www.apilayer.net/api/live?access_key=304ab1caae3a0cff5303cab9619d6140&format=1"
}

case class CurrencyResponse(
  quotes: Map[String, BigDecimal]
)