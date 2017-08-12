package com.scanner.service.currency

import com.scanner.service.currency.ApilayerService._
import io.circe.generic.auto._
import io.circe.parser.parse

import scala.concurrent.Future
import scala.io.Source

import scala.concurrent.ExecutionContext.Implicits.global

class ApilayerService {

  def fetchCurrencies(): Future[CurrencyResponse] = {
    for {
      content <- Future(Source.fromURL(apiUrl, "UTF-8").mkString)
      json <- Future.fromTry(parse(content).toTry)
      currencyResponse <- Future.fromTry(json.as[CurrencyResponse].toTry)
    } yield currencyResponse
  }

}

object ApilayerService {
  val accessKey = "304ab1caae3a0cff5303cab9619d6140&format=1"
  val apiUrl = s"http://www.apilayer.net/api/live?access_key=$accessKey"

  case class CurrencyResponse(quotes: Map[String, BigDecimal])
}