package com.scanner.service.core.marshal

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by IGorbylov on 09.03.2017.
  */
object BasicUnmarshallers {
  implicit def toLocalDateUnmarshaller = new FromStringUnmarshaller[LocalDate] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[LocalDate] =
      Future.successful(LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
  }
  implicit def toLocalDateTimeUnmarshaller = new FromStringUnmarshaller[LocalDateTime] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[LocalDateTime] =
      Future.successful(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
  }
}
