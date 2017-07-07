package com.scanner.service.api.message

import java.time.LocalDate

import com.scanner.query.api.{Airline, Direction}
import com.scanner.service.api.http.ImperativeRequestContext

/**
  * Created by igorbylov on 28.06.17.
  */
sailed trait ApiMessage

case class OneWayRequest(
  context: ImperativeRequestContext,
  params: RequestParams
) extends ApiMessage
case class RequestParams (
 origin: String,
 arrival: String,
 from: LocalDate,
 to: LocalDate,
 airlines: List[Airline],
 currency: String
)

case class FailureMessage(
 status: Int,
 message: String
) extends ApiMessage

case object CollectOneWayFlights extends ApiMessage

case class BuildPath(
  context: ImperativeRequestContext,
  params: RequestParams,
  direction: Direction
)

case class ResolveAirportMessage(
  context: ImperativeRequestContext,
  params: RequestParams,
  direction: Direction
)