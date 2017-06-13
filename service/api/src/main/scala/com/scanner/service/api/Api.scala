package com.scanner.service.api

import java.time.LocalDate

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.scanner.query.api.{Airline, GetOneWayFlightsRequest, GetOneWayFlightsResponse}
import com.scanner.query.core.Response

import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.CirceSupport
import akka.pattern.ask
import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.json.BasicCodecs._

/**
  * Created by IGorbylov on 09.03.2017.
  */
trait Api extends CirceSupport {

  implicit val askTimeout = Timeout(10 seconds)

  def routes(apiService: ActorRef): Route = encodeResponse {
    path("scan") {
      parameters('origin, 'arrival, 'start.as[LocalDate], 'end.as[LocalDate], 'airline.as[Airline].*, 'currency) { (origin, arrival, start, end, airlines, currency) =>
        withRequestTimeout(10 seconds, request => HttpResponse(StatusCodes.EnhanceYourCalm, entity = "Request timeout")) {
          validate(checkParams(origin, arrival, start, end, currency), "Bad request parameters") {
            completeQuery(apiService ? GetOneWayFlightsRequest(origin, arrival, start, end, airlines.toSeq, currency)) {
              case GetOneWayFlightsResponse(flights) => complete(flights)
            }
          }
        }
      }
    }
  }

  private def checkParams(origin: String, arrival: String, start: LocalDate, end: LocalDate, currency: String): Boolean = {
    origin.length == 3 &&
    arrival.length == 3 &&
    start.isAfter(LocalDate.now().minusDays(1)) &&
    start.isBefore(end) &&
    end.isBefore(LocalDate.now().plusYears(1)) &&
    currency.length == 3
  }

  def completeQuery(response: Future[Any])(happyPath: PartialFunction[Response, Route]): Route = {
    onComplete(response.mapTo[Response]){ tryResult =>
      tryResult.fold(
        _ => complete(InternalServerError),
        resp => happyPath(resp)
      )
    }
  }
}