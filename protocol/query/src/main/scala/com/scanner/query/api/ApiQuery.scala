package com.scanner.query.api

import java.time.{LocalDate, LocalDateTime}

import com.scanner.query.core.{Query, Response}

/**
  * Created by IGorbylov on 09.03.2017.
  */
sealed trait ApiQuery extends Query
sealed trait ApiResponse extends Response

case class GetFlightsMessage(
  requestId: String,
  pathId: String,
  origin: Airport,
  arrival: Airport,
  from: LocalDate,
  to: LocalDate,
  airlines: Seq[Airline],
  currency: String
) extends ApiQuery
case class GetOneWayFlightsResponse(
  flights: List[FlightView]
) extends ApiResponse

case object GetConnectionsQuery extends ApiQuery
case class GetConnectionsResponse(connections: Map[String, List[String]]) extends ApiResponse

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

case class ResolveAirportMessage(
  requestId: String,
  params: RequestParams
) extends ApiQuery

case class BuildPathMessage(
  requestId: String,
  origin: Airport,
  arrival: Airport,
  params: RequestParams
) extends ApiQuery

case class BuildGraph(airportsState: Map[String, Airport]) extends ApiQuery

case class FailureMessage(
  status: Int,
  message: String
)

case class Airport(
  iata: String,
  name: String,
  lat: BigDecimal,
  lng: BigDecimal
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

case class Country(name: String, code: String)
case class City(name: String, code: String)
