package com.scanner.service.api

import java.time.LocalDate

import akka.actor.{ActorRef, ActorSelection}
import akka.http.scaladsl.server.Route
import com.scanner.service.core.web.Api
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.pattern.ask
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse}
import com.scanner.query.core.Response

import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._
import com.scanner.service.core.collections.Utils.TraversableOfFutures

/**
  * Created by IGorbylov on 09.03.2017.
  */
trait ScannerApi extends Api {

  def route(wizzairService: ActorRef): Route = encodeResponse {
    path("scan") {
      parameters('origin, 'arrival, 'start.as[LocalDate], 'end.as[LocalDate], 'airline.as[Airline].*, 'currency) { (origin, arrival, start, end, airlines, currency) => {
        validate(checkParams(origin, arrival, start, end, currency), "Bad request parameters") {
          val result = airlines
            .map(_ => wizzairService ? GetOneWayFlightsQuery(origin, arrival, start, end, currency))
            .futureSequence()
            .map{
              case GetOneWayFlightsResponse(flights) => flights
              case  _ => Iterable()
            }
            .flatMap(_ => _)

          complete(result)
        }
      }
      }
    }
  }

  private def checkParams(origin: String, arrival: String, start: LocalDate, end: LocalDate, currency: String): Boolean = {
    origin.length == 3 &&
      arrival.length == 3 &&
      start.isAfter(LocalDate.now()) &&
      start.isBefore(end) &&
      end.isBefore(LocalDate.now().plusYears(1)) &&
      currency.length == 3 &&
  }
}


sealed trait Airline

case object Wizzair extends Airline