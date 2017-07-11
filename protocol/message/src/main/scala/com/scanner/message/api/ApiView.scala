package com.scanner.message.api

import java.time.{LocalDate, LocalDateTime}

/**
  * Created by igorbylov on 11.07.17.
  */
trait ApiView

case class FlightView(
  flightNumber: String,
  origin: String,
  arrival: String,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  airline: String, // TODO use Airline trait
  price: BigDecimal,
  currency: String
)

case class AirportView(
  iata: String,
  name: String,
  lat: BigDecimal,
  lon: BigDecimal
)

case class RequestParams(
  origin: String,
  arrival: String,
  from: LocalDate,
  to: LocalDate,
  airlines: List[Airline],
  currency: String,
  direction: Direction
)

sealed trait Airline
case object Wizzair extends Airline

sealed trait Direction
case object OneWay extends Direction
case object BothWays extends Direction



