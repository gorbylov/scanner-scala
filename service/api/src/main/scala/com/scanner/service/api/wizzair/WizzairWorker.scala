package com.scanner.service.api.wizzair

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse, GetOneWayFlightsView}

import scala.io.Source
import com.scanner.service.api.wizzair.WizzairWorker._
import io.circe.parser._
import io.circe.generic.auto._
/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor {

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) =>
      //sender ! GetOneWayFlightsResponse(flights(origin, arrival, start, end))
      flights(origin, arrival, start, end)
  }

  def flights(origin: String, arrival: String, start: LocalDate, end: LocalDate) = {
    val content = Source.fromURL(s"$TIMETABLE_ROOT?departureIATA=$origin&arrivalIATA=$arrival&year=2017&month=10")
      .mkString
    val result = parse(content)
      .flatMap(_.as[List[WizzairTimetableResponse]])
      .map(response =>
        GetOneWayFlightsView("key", response.head.DepartureStationCode, response.head.ArrivalStationCode, LocalDateTime
          .now(),
          LocalDateTime.now(), "WIZZAIR", 11, "UAH")
      ).getOrElse(null)
    println(result)
  }
}

case class WizzairTimetableResponse(
  ArrivalStationCode: String, //BUD
  DepartureStationCode: String, //IEV
  MinimumPrice: Option[String], // 2 090,00UAH
  Flights: List[WizzairFlightInfoDto]
)

case class WizzairFlightInfoDto(
  CarrierCode: String, //W6
  FlightNumber: String, //6275
  STA: String, // h:mm
  STD: String  // h:mm
)

object WizzairWorker {
  val API_ROOT = "https://cdn.static.wizzair.com"
  val TIMETABLE_ROOT = s"$API_ROOT/en-GB/TimeTableAjax"
  val CONN_TIMEOUT = 10000
  val READ_TIMEOUT = 10000
}
