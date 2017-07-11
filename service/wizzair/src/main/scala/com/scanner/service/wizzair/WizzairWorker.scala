package com.scanner.service.wizzair

import java.time.{LocalDate, LocalDateTime, LocalTime}

import akka.actor.{Actor, ActorLogging}
import com.scanner.message.wizzair._
import com.scanner.service.core.utils.SequenceUtils.TrySequence

import scala.io.Source
import scala.util.Try
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.service.wizzair.json.WizzairCodecs._
import com.scanner.service.core.utils.Exceptions._

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor with ActorLogging {
  import WizzairWorker._

  val wizzairCurrenciesToISO = Map(
    "₪" -> "ILS", "€" -> "EUR", "£" -> "GBP", "kr" -> "NOK", "zł" -> "PLN", "Kč" -> "CZK",
    "Ft" -> "HUF", "KM" -> "BAM", "MKD" -> "MKD", "din" -> "RSD", "lv" -> "BGN", "lei" -> "RON",
    "SFr" -> "CHF", "UAH" -> "UAH"
  )

  override def receive: Receive = {
    case GetWizzairFlightsMessage(origin, arrival, year, month) =>
      val response = flights(origin, arrival, year, month).fold[WizzairResponse](
        error => {
          log.error(s"An error occurred while processing wizzair flights\n${error.mkString()}")
          WizzairFailure(error, "message")
        },
        flights => GetWizzairFlightsResponse(flights)
      )
      sender ! response
  }

  def flights(origin: String, arrival: String, year: Int, month: Int): Try[List[WizzairFlightView]] = {
    val url = s"$TIMETABLE_ROOT?departureIATA=$origin&arrivalIATA=$arrival&year=$year&month=$month"
    // getting json content and converting to plain object
    val maybeResponses = for {
      content <- Try(Source.fromURL(url, "UTF-8").mkString) // TODO Future
      json <- parse(content).toTry
      response <- json.as[List[WizzairTimetableResponse]].toTry
    } yield response
    // mapping responses to views
    maybeResponses.map { responses =>
      responses
        .filter(_.MinimumPrice.isDefined) // TODO filters here doesn't look as a good idea
        .filter{ response =>
          val result = wizzairCurrenciesToISO.contains(response.MinimumPrice.get.replaceAll("[0-9.,]", "").trim)
          if (!result) log.error(s"Unknown currency ${response.MinimumPrice.get}")
          result
        }
        .map { response =>
          response.Flights.map {
            case WizzairFlightInfoDto(carrierCode, flightNumber, depTime, arrTime) =>
              WizzairFlightView(
                s"$carrierCode $flightNumber",
                response.DepartureStationCode,
                response.ArrivalStationCode,
                LocalDateTime.of(response.Date, depTime),
                LocalDateTime.of(response.Date, arrTime),
                BigDecimal(response.MinimumPrice.get.replaceAll("[^0-9.]", "")),
                wizzairCurrenciesToISO(response.MinimumPrice.get.replaceAll("[0-9.,]", "").trim)
              )
          }
        }
    }
      .map(_.flatten)
  }
}

case class WizzairTimetableResponse(
  ArrivalStationCode: String,         //BUD
  DepartureStationCode: String,       //IEV
  MinimumPrice: Option[String],       // 2 090,00UAH
  Date: LocalDate,                    // 20170626
  Flights: List[WizzairFlightInfoDto]
)

case class WizzairFlightInfoDto(
  CarrierCode: String,    // W6
  FlightNumber: String,   // 6275
  STA: LocalTime,         // hh:mm
  STD: LocalTime          // hh:mm
)

object WizzairWorker {
  val API_ROOT = "https://cdn.static.wizzair.com"
  val TIMETABLE_ROOT = s"$API_ROOT/en-GB/TimeTableAjax"
  val CONN_TIMEOUT = 10000
  val READ_TIMEOUT = 10000
}
