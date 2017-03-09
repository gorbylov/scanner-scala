package com.scanner.service.api.marshal

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.stream.Materializer
import com.scanner.service.api.{Airline, Wizzair}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by IGorbylov on 09.03.2017.
  */
object ApiUnmarshallers {

  implicit def toAirlineTimeUnmarshaller = new FromStringUnmarshaller[Airline] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[Airline] =
      Future.successful(value match {
        case "wizzair" => Wizzair
      })
  }
}
