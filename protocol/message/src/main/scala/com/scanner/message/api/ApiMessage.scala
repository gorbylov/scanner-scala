package com.scanner.message.api

import java.time.{LocalDate, LocalDateTime}

import com.scanner.message.core.{Message, Response, TestMessage}

/**
  * Created by IGorbylov on 09.03.2017.
  */
sealed trait ApiMessage extends Message
sealed trait ApiResponse extends Response

case class GetFlightsMessage(
  requestId: String,
  pathId: String,
  origin: AirportView,
  arrival: AirportView,
  from: LocalDate,
  to: LocalDate,
  airlines: Seq[Airline],
  currency: String
) extends ApiMessage

case class GetOneWayFlightsResponse(
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
  origin: AirportView,
  arrival: AirportView,
  params: RequestParams
) extends ApiMessage

case class BuildGraphMessage(airportsState: Map[String, AirportView]) extends ApiMessage
