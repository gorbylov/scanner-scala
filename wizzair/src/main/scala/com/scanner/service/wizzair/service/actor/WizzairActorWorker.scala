package com.scanner.service.wizzair.service.actor

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.core.utils.Exceptions.ExceptionUtils
import com.scanner.protocol.wizzair._
import com.scanner.service.wizzair.service.WizzairService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class WizzairActorWorker(wizzairService: WizzairService) extends Actor
  with ActorLogging {

  override def receive: Receive = {
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
    val futureTimetable = wizzairService.fetchFlights(
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

object WizzairActorWorker {
  def props(wizzairService: WizzairService): Props = Props(new WizzairActorWorker(wizzairService))
}
