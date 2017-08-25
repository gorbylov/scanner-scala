package com.scanner.service.wizzair.actor

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.message.core.Message
import com.scanner.message.wizzair._
import com.scanner.service.core.actor.ActorService
import com.scanner.service.core.utils.Exceptions.ExceptionUtils
import com.scanner.service.wizzair.json.WizzairCodecs._
import com.scanner.service.wizzair.utils.WizzairApiFetcher
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class WizzairWorker(wizzairApiFetcher: WizzairApiFetcher) extends Actor
  with ActorLogging
  with ActorService {

  override def handleMessage: Function[Message, Unit] = {

    case GetWizzairFlightsMessage(origin, arrival, date) =>
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

  def flights(origin: String, arrival: String, date: LocalDate): Future[List[WizzairFlightView]] = {
    val futureTimetable = wizzairApiFetcher.fetchTimetableFlights(
      origin,
      arrival,
      date.withDayOfMonth(1),
      date.withDayOfMonth(date.lengthOfMonth())
    )
    // mapping responses to views
    futureTimetable.map { responses =>
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
}

object WizzairWorker {
  def props(wizzairApiFetcher: WizzairApiFetcher): Props =
    Props(new WizzairWorker(wizzairApiFetcher))
}
