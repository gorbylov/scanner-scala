package com.scanner.query.api

import java.time.{LocalDate, LocalDateTime}

import com.scanner.query.core.{Query, Response}

/**
  * Created by IGorbylov on 09.03.2017.
  */
sealed trait ApiQuery extends Query
sealed trait ApiResponse extends Response

case class GetOneWayFlightsRequest(
  origin: String,
  arrival: String,
  start: LocalDate,
  end: LocalDate,
  airlines: Seq[Airline],
  currency: String
) extends ApiQuery
case class GetOneWayFlightsResponse(
  flights: List[GetOneWayFlightsView]
) extends ApiResponse

case object GetConnectionsQuery extends ApiQuery
case class GetConnectionsResponse(connections: Map[String, List[String]]) extends ApiResponse

case class GetOneWayFlightsView(
  flightNumber: String,
  origin: String,
  arrival: String,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  airline: String, // TODO use Airline trait
  price: BigDecimal,
  currency: String
)

case class Airport(
  name: String,
  code: String,
  lat: Double,
  lng: Double
//  country: Country,
//  city: City,
//  utc: Double
)

sealed trait Airline
case object Wizzair extends Airline

case class Country(name: String, code: String)
case class City(name: String, code: String)
