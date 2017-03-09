package com.scanner.query.api

import java.time.{LocalDate, LocalDateTime}

import com.scanner.query.core.{Query, Response}

/**
  * Created by IGorbylov on 09.03.2017.
  */
sealed trait ApiQueries extends Query
sealed trait ApiResponse extends Response

case class GetOneWayFlightsQuery(
  origin: String,
  arrival: String,
  start: LocalDate,
  end: LocalDate,
  currency: String
)
case class GetOneWayFlightsResponse(flights: Seq[GetOneWayFlightsView])

case class GetOneWayFlightsView(
  key: String,
  origin: Airport,
  arrival: Airport,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  airline: String,
  price: BigDecimal,
  currency: String
)

case class Airport(
  name: String,
  code: String,
  lat: Double,
  lng: Double,
  country: Country,
  city: City,
  utc: Double
)
case class Country(name: String, code: String)
case class City(name: String, code: String)
