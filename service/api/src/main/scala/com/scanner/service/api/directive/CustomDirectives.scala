package com.scanner.service.api.directive

import java.time.LocalDate

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server._
import com.scanner.query.api.Airline

import scala.concurrent.Promise
import com.scanner.service.core.json.BasicCodecs._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import com.scanner.service.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._

/**
  * Created by Iurii on 22-06-2017.
  */
object CustomDirectives {

  def oneWayRequest: Directive[(String, String, LocalDate, LocalDate, Iterable[Airline], String)] = {
    parameters('origin, 'arrival, 'start.as[LocalDate], 'end.as[LocalDate], 'airline.as[Airline].*, 'currency)
  }

  // an imperative wrapper for request context
  private final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
    private implicit val ec = ctx.executionContext
    def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)
    def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)
  }
  // a custom directive
  def tell(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
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
