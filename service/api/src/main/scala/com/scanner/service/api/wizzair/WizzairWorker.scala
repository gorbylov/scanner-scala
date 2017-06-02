package com.scanner.service.api.wizzair

import java.time.{LocalDate, LocalDateTime}

import akka.actor.Actor
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse, GetOneWayFlightsView}
import com.scanner.service.core.utils.Dates._
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.service.api.wizzair.WizzairWorker._

import scala.io.Source
import scala.util.Try
import com.scanner.service.core.utils.SequenceUtils.TrySequence

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor {

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) =>
      sender ! GetOneWayFlightsResponse(flights(origin, arrival, start, end))
  }

  def flights(origin: String, arrival: String, from: LocalDate, to: LocalDate): Seq[GetOneWayFlightsView] = {
    def buildUrl: LocalDate => String = date =>
      s"$TIMETABLE_ROOT?departureIATA=$origin&arrivalIATA=$arrival&year=${date.getYear}&month=${date.getMonth.getValue}"
    def mapResponse2View: WizzairTimetableResponse => List[GetOneWayFlightsView] = {
      case WizzairTimetableResponse(arr, dep, Some(price), flights) =>
        for {
          WizzairFlightInfoDto(carrierCode, flightNumber, depDate, arrDate) <- flights
        } yield GetOneWayFlightsView(
          s"$carrierCode $flightNumber",
          dep,
          arr,
          LocalDateTime.now(), // TODO deserialize
          LocalDateTime.now(), // TODO deserialize
          "WIZZAIR",
          BigDecimal(price.replaceAll("[^0-9.]", "")), // TODO convert currency
          "UAH"
        )
    }
    // getting json content and converting to plain object
    val maybeResponses = (from -> to).toMonthsInterval()
      .map(date => buildUrl(date))
      .map { url =>
        for {
          content <- Try(Source.fromURL(url).mkString)
          json <- parse(content).toTry
          response <- json.as[List[WizzairTimetableResponse]].toTry
        } yield response
      }
      .sequence()
      // mapping responses to views
      val views = maybeResponses.map{ lists =>
        for {
          list <- lists
          response <- list if response.MinimumPrice.isDefined
          view <- mapResponse2View(response)
        } yield view
      }
      .recover {
        case e =>
          // log error
          List()
      }
      .getOrElse(List())
    
    views
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
