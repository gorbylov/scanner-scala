package com.scanner.query.wizzair

import java.time.{LocalDate, LocalDateTime}

import com.scanner.query.core.{Query, Response}

/**
  * Created by Iurii on 11-06-2017.
  */
sealed trait WizzairQuery extends Query
sealed trait WizzairResponse extends Response

case class GetWizzairFlightsQuery(
  origin: String,
  arrival: String,
  from: LocalDate,
  to: LocalDate
) extends WizzairQuery
case class GetWizzairFlightsResponse(
  flights: List[WizzairFlightView]
)

case class WizzairFlightView(
  flightNumber: String,
  origin: String,
  arrival: String,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  price: BigDecimal,
  currency: String
)

