package com.scanner.service.wizzair

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.message.core.Message
import com.scanner.message.wizzair._
import com.scanner.service.core.actor.ActorService
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.service.wizzair.json.WizzairCodecs._
import com.scanner.service.wizzair.WizzairService.apiRoot
import com.scanner.service.core.utils.Exceptions.ExceptionUtils

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalaj.http.Http

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor
  with ActorLogging
  with ActorService {

  override def handleMessage: Function[Message, Unit] = {

    case GetWizzairFlightsMessage(origin, arrival,date) => // TODO send LocalDate instead of year and month
      val currentSender = sender()
      flights(origin, arrival,date)
        .map(GetWizzairFlightsResponse)
        .onComplete{
          case Success(response) =>
            currentSender ! response
          case Failure(error) =>
            log.error(error.mkString())
        }
  }

  def flights(origin: String, arrival: String, date:LocalDate): Future[List[WizzairFlightView]] = {
    val (startDay,endDay) = makeDateForRequest(date)
    val data = s"""
      |{
      |  "flightList": [
      |    {
      |      "departureStation": "$origin",
      |      "arrivalStation": "$arrival",
      |      "from": "$startDay",
      |      "to": "$endDay"
      |    },
      |    {
      |      "departureStation": "$arrival",
      |      "arrivalStation": "$origin",
      |      "from": "$startDay",
      |      "to": "$endDay"
      |    }
      |  ],
      |  "priceType": "regular"
      |}
    """.stripMargin



    // getting json content and converting to plain object
    val futureResponses = for {
      content <- Future(Http(s"$apiRoot/search/timetable").method("POST").postData(data).header("Content-Type", "application/json").asString.body)
      json <- Future.fromTry(parse(content).toTry)
      response <- Future.fromTry(json.as[WizzairTimetableResponse].toTry)
    } yield response
    // mapping responses to views
    futureResponses.map { responses =>
      for {
        flightDto <- responses.outboundFlights
        date <- flightDto.departureDates
      } yield WizzairFlightView(
        "empty",
        flightDto.departureStation,
        flightDto.arrivalStation,
        date,
        date, // TODO find out how to get arrival date
        flightDto.price.amount,
        flightDto.price.currencyCode
      )
    }
  }
  def makeDateForRequest(date: LocalDate): (String,String) ={
    val d1 = date.withDayOfMonth(1)
    val d2 = date.withDayOfMonth(date.lengthOfMonth())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    (d1.format(formatter), d2.format(formatter))
  }
}

private case class WizzairTimetableResponse(
  outboundFlights: List[WizzairFlightInfoDto],
  returnFlights: List[WizzairFlightInfoDto]
)

private case class WizzairFlightInfoDto(
  departureStation: String,
  arrivalStation: String,
  departureDate: LocalDateTime,
  price: WizzairFlightPriceDto,
  priceType: String,
  departureDates: List[LocalDateTime],
  classOfService: String,
  hasMacFlight: Boolean
)

private case class WizzairFlightPriceDto(
  amount: Int,
  currencyCode: String
)

object WizzairWorker {

  def props(): Props = Props(new WizzairWorker)

  val CONN_TIMEOUT = 10000
  val READ_TIMEOUT = 10000
}
