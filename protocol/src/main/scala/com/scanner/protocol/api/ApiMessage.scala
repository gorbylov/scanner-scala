package com.scanner.protocol.api

import java.time.{LocalDate, LocalDateTime}

import com.scanner.protocol.core.{Message, Response, TestMessage}

/**
  * Created by IGorbylov on 09.03.2017.
  */
sealed trait ApiMessage extends Message
sealed trait ApiResponse extends Response

case class RequestResponse(
  requestId: String,
  flights: List[List[FlightView]]
) extends Response

case class AggregateFlights(
  requestId: String,
  pathsCount:Int,
  flights: List[List[FlightView]]
) extends Message

case class FetchFlightsForPathMessage(
  path: List[(Airport, Airport)],
  from: LocalDate,
  to: LocalDate,
  airlines: Seq[Airline],
  currency: String,
  direction: Direction
) extends ApiMessage

case class GetFlightsMessage(
  stepIndex: Int,
  stepsCount: Int,
  origin: Airport,
  arrival: Airport,
  from: LocalDate,
  to: LocalDate,
  currency: String
) extends ApiMessage

case class GetFlightsResponse(
  stepIndex: Int,
  stepsCount: Int,
  flights: List[FlightView]
) extends ApiResponse

case object GetConnectionsMessage extends ApiMessage
case class GetConnectionsResponse(connections: Map[String, List[String]]) extends ApiResponse

case object FetchAirportsMessage extends ApiMessage

case class ResolveAirportMessage(
  requestId: String,
  params: RequestParams
) extends ApiMessage

case class BuildPathMessage(
  requestId: String,
  origin: Airport,
  arrival: Airport,
  params: RequestParams
) extends ApiMessage

case class BuildGraphMessage(airportsState: Map[String, Airport]) extends ApiMessage
