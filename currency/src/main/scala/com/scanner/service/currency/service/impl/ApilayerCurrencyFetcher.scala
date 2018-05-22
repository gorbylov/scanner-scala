package com.scanner.service.currency.service.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.util.ByteString
import com.scanner.service.currency.CurrencyConfig
import com.scanner.service.currency.model.Currencies
import com.scanner.service.currency.service.CurrencyFetcher
import com.scanner.service.currency.service.impl.ApilayerCurrencyFetcher.ApilayerCurrencies
import io.circe.parser.parse
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class ApilayerCurrencyFetcher()(implicit ec: ExecutionContext, as: ActorSystem, m: Materializer) extends CurrencyFetcher
  with CurrencyConfig {

  import ApilayerCurrencyFetcher.apiUri

  /** @inheritdoc */
  override def fetch(): Future[Currencies] = {
    for {
      response  <- Http().singleRequest(HttpRequest(uri = s"$apiUri?access_key=$apilayerKey"))
      content   <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      json      <- Future.fromTry(parse(content).toTry)
      result    <- Future.fromTry(json.as[ApilayerCurrencies].toTry)
    } yield Currencies(result.quotes)
  }
}

object ApilayerCurrencyFetcher {
  val apiUri = s"http://www.apilayer.net/api/live"

  case class ApilayerCurrencies(quotes: Map[String, BigDecimal])
}
