package com.scanner.service.wizzair.json

import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter

import io.circe.{Decoder, HCursor}

/**
  * Created by IGorbylov on 08.06.2017.
  */
object WizzairCodecs {

  implicit def localDateDecoder: Decoder[LocalDate] =
    (c: HCursor) => c.as[String].map(LocalDate.parse(_, DateTimeFormatter.ofPattern("yyyyMMdd")))

  implicit def localTimeDecoder: Decoder[LocalTime] =
    (c: HCursor) => c.as[String].map(LocalTime.parse(_, DateTimeFormatter.ofPattern("HH:mm")))

}
