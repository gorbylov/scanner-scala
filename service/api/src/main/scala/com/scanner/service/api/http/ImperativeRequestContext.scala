package com.scanner.service.api.http

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{RequestContext, RouteResult}

import scala.concurrent.Promise

/**
  * Created by igorbylov on 27.06.17.
  */
class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {

  private implicit val ec = ctx.executionContext

  def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)

  def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)

}