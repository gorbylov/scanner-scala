package com.scanner.message.api

import com.scanner.message.core.TestMessage

/**
  * Created by igorbylov on 11.07.17.
  */
sealed trait ApiTestMessage extends TestMessage

case object GetAirportsStateMessage extends ApiTestMessage
case class GetAirportsStateResponse(airportsState: Map[String, AirportView]) extends ApiTestMessage

case object GraphIsEmptyMessage extends ApiTestMessage
case class GraphIsEmptyResponse(empty: Boolean) extends ApiTestMessage

