package com.scanner.service.api.http

import java.time.LocalDate

import akka.http.scaladsl.marshalling.{ToEntityMarshaller, ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server._
import com.scanner.query.api.Airline

import scala.concurrent.Promise
import akka.http.scaladsl.server.Directives._
import com.scanner.service.api.Api.{FailureMessage, RequestParams}
import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._

/**
  * Created by Iurii on 22-06-2017.
  */
object CustomDirectives {

  def requestParams: Directive[Tuple1[RequestParams]] = {
    parameters('origin, 'arrival, 'from.as[LocalDate], 'to.as[LocalDate], 'airline.as[Airline].*, 'currency)
      .tmap {
        case (origin, arrival, from, to, airlines, currency) =>
          RequestParams(origin, arrival, from, to, airlines.toList, currency)
      }
  }


  def tell(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }

  def validate(requestParams: RequestParams): Directive[Unit] = {
    import com.scanner.service.api.marshal.ApiMarshallers.failureMessageMarshaller
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

 /* def validate(params: Seq[(Any, Any => Option[String])]): Directive[Unit] = {
    val maybeError = params
      .map{case (value, predicate) => predicate(value)}
      .foldLeft[Option[String]](None){
        case (None, Some(error)) => Some(error)
        case (Some(msg1), Some(msg2)) => Some(s"$msg1\n$msg2")
        case (Some(error), None) => Some(error)
        case (None, None) => None
      }
    Directive { inner =>
      maybeError match {
        case Some(msg) => reject(ValidationRejection(msg))
        case None => inner(())
      }
    }
  }*/
}
