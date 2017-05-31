package com.scanner.service.api.wizzair

import java.time.LocalDate

import akka.actor.Actor
import com.scanner.query.api.GetOneWayFlightsQuery
import com.scanner.service.api.wizzair.WizzairWorker._
import com.scanner.service.core.utils.Dates._
import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source
import scala.util.Try
/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor {

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) =>
      //sender ! GetOneWayFlightsResponse(flights(origin, arrival, start, end))
      flights(origin, arrival, start, end)
  }

  def flights(origin: String, arrival: String, from: LocalDate, to: LocalDate) = {
    val result = for {
      date <- (from -> to).toMonthsInterval()
      content <- Try(Source.fromURL(s"$TIMETABLE_ROOT?departureIATA=$origin&arrivalIATA=$arrival&year=${date.getYear}&month=${date.getMonth.getValue}").mkString)
      list <- parse(content).flatMap(_.as[List[WizzairTimetableResponse]]).toTry
    } yield list
    result.


//    val result = (from -> to).toMonthsInterval()
//      .flatMap(date =>
//        Try(Source.fromURL(s"$TIMETABLE_ROOT?departureIATA=$origin&arrivalIATA=$arrival&year=${date.getYear}&month=${date.getMonth.getValue}").mkString)
//          .flatMap(content => parse(content).flatMap(_.as[List[WizzairTimetableResponse]]).toTry)
//          .fold(
//            error =>
//              //log here
//              Nil,
//            response => response
//          )
//      )
//
//
    println(result)
  }
}

case class WizzairTimetableResponse(
  ArrivalStationCode: String,         //BUD
  DepartureStationCode: String,       //IEV
  MinimumPrice: Option[String],       // 2 090,00UAH
  Flights: List[WizzairFlightInfoDto]
)

case class WizzairFlightInfoDto(
  CarrierCode: String,    // W6
  FlightNumber: String,   // 6275
  STA: String,            // h:mm
  STD: String             // h:mm
)

object WizzairWorker {
  val API_ROOT = "https://cdn.static.wizzair.com"
  val TIMETABLE_ROOT = s"$API_ROOT/en-GB/TimeTableAjax"
  val CONN_TIMEOUT = 10000
  val READ_TIMEOUT = 10000
}
