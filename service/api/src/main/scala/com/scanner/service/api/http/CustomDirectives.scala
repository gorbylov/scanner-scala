package com.scanner.service.api.http

import java.time.LocalDate

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server._
import com.scanner.query.api.{Airline, Direction, FailureMessage, RequestParams}

import scala.concurrent.{Await, Promise}
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._
import com.scanner.service.api.marshal.ApiMarshallers.failureMessageMarshaller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Iurii on 22-06-2017.
  */
object CustomDirectives {

  def requestParams: Directive[Tuple1[RequestParams]] = {
    parameters(
      'origin,
      'arrival,
      'from.as[LocalDate],
      'to.as[LocalDate],
      'airline.as[Airline].*,
      'currency,
      'direction.as[Direction]
    )
      .tmap {
        case (origin, arrival, from, to, airlines, currency, direction) =>
          RequestParams(origin, arrival, from, to, airlines.toList, currency, direction)
      }
  }


  def tell(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }

  def validate(requestParams: RequestParams): Directive[Unit] = {

    Directive { inner =>
      var errorMessages = List[String]()
      if (requestParams.origin.length != 3) {
        errorMessages =  "Origin chars length should be 3." :: errorMessages
      }
      if (requestParams.arrival.length != 3) {
        errorMessages =  "Arrival chars length should be 3." :: errorMessages
      }
      if (requestParams.currency.length != 3) {
        errorMessages =  "Currency chars length should be 3." :: errorMessages
      }
      if (requestParams.from.isBefore(LocalDate.now())) {
        errorMessages =  "From date can't be in the past." :: errorMessages
      }
      if (requestParams.from.isAfter(requestParams.to)) {
        errorMessages =  "From date can't be after to date." :: errorMessages
      }
      if (requestParams.to.isAfter(LocalDate.now().plusYears(1))) {
        errorMessages =  "To date can't be later then 1 year." :: errorMessages
      }
      errorMessages match {
        case Nil => inner(())
        case errors => complete(StatusCodes.BadRequest -> FailureMessage(400, errors.mkString(" ")))
      }
    }
  }

  def requestTimeout(duration: Duration): Directive[Unit] = {
    val handler: HttpRequest => HttpResponse = { _ =>
      HttpResponse(
        status = StatusCodes.RequestTimeout,
        entity = Await.result(Marshal(FailureMessage(408, "Request timeout")).to[MessageEntity], 2 seconds)
      )
    }
    withRequestTimeout(duration, handler)
  }
}
