package com.scanner.service.api

import java.time.LocalDate

import akka.actor.{ActorRef}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse}
import com.scanner.query.core.Response

import scala.concurrent.Future
import scala.util.{Failure, Success}
import de.heikoseeberger.akkahttpcirce.CirceSupport

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

  implicit val askTimeout = Timeout(5 seconds)

  def routes(wizzairService: ActorRef): Route = encodeResponse {
    path("scan") {
      parameters('origin, 'arrival, 'start.as[LocalDate], 'end.as[LocalDate], 'airline.as[Airline].*, 'currency) { (origin, arrival, start, end, airlines, currency) => {
        validate(checkParams(origin, arrival, start, end, currency), "Bad request parameters") {
          val futureResult = Future.sequence(
            airlines
              .map{
                case Wizzair => (wizzairService ? GetOneWayFlightsQuery(origin, arrival, start, end, currency))
                  .mapTo[GetOneWayFlightsResponse]
                  .map(it => it.flights)
              }
          )
          .map(it => it.flatten)

          onComplete(futureResult){
            case Success(flights) => complete(flights)
            case Failure(e) => complete(StatusCodes.InternalServerError)
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


sealed trait Airline
case object Wizzair extends Airline