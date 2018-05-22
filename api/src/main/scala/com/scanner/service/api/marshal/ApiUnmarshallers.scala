package com.scanner.service.api.marshal

import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.stream.Materializer
import com.scanner.protocol.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by IGorbylov on 09.03.2017.
  */
object ApiUnmarshallers {

  implicit def airlineUnmarshaller = new FromStringUnmarshaller[Airline] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[Airline] =
      Future(value.toLowerCase match {
        case "wizzair" => Wizzair
      })
  }

  implicit def directionUnmarshaller = new FromStringUnmarshaller[Direction] { // TODO typeclass
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[Direction] =
      Future(value.toLowerCase match {
        case "one" => OneWay
        case "both" => BothWays
      })
  }
}
