package com.scanner.core.json

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}


/**
  * Created by IGorbylov on 27.03.2017.
  */
object BasicCodecs {

  implicit def localDateTimeEncoder: Encoder[LocalDateTime] =
    (a: LocalDateTime) => a.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).asJson
  implicit def localDateTimeDecoder: Decoder[LocalDateTime] =
    (c: HCursor) => c.as[String].map(LocalDateTime.parse(_, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))

  implicit def localDateEncoder: Encoder[LocalDate] =
    (a: LocalDate) => a.toString.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).asJson
  implicit def localDateDecoder: Decoder[LocalDate] =
    (c: HCursor) => c.as[String].map(LocalDate.parse(_, DateTimeFormatter.ofPattern("yyyy-MM-dd")))

}