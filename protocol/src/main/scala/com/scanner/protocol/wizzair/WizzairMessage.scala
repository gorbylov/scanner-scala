package com.scanner.protocol.wizzair

import java.time.LocalDate

import com.scanner.protocol.core.{Message, Response}

/**
  * Created by Iurii on 11-06-2017.
  */
sealed trait WizzairMessage extends Message
sealed trait WizzairResponse extends Response

case class WizzairFailure(error: Throwable, message: String) extends WizzairResponse

case class GetWizzairFlightsMessage(
  origin: String,
  arrival: String,
  date: LocalDate
) extends WizzairMessage
case class GetWizzairFlightsResponse(
  flights: List[WizzairFlightView]
) extends WizzairResponse

