package com.scanner.service.api

import java.time.LocalDate

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import de.heikoseeberger.akkahttpcirce.CirceSupport
import com.scanner.service.api.directive.CustomDirectives.oneWayRequest
import com.scanner.service.api.directive.CustomDirectives.tell

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.json.BasicCodecs._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._

/**
  * Created by IGorbylov on 09.03.2017.
  */
trait Api extends CirceSupport {

  implicit val askTimeout = Timeout(10 seconds)

  def routes(apiService: ActorRef): Route = encodeResponse {
    path("scan") {
      get {
        oneWayRequest { (origin, arrival, start, end, airlines, currency) =>
          validate(checkParams(origin, arrival, start, end, currency), "Validation error.") {
            withRequestTimeout(10 seconds, _ => HttpResponse(StatusCodes.RequestTimeout, entity = "Request timeout")) {
              tell { ctx =>
                println("aaa")
              }
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

  /*def completeQuery(response: Future[Any])(happyPath: PartialFunction[Response, Route]): Route = {
    onComplete(response.mapTo[Response]){ tryResult =>
      tryResult.fold(
        _ => complete(InternalServerError),
        resp => happyPath(resp)
      )
    }
  }*/
}