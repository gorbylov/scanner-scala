package com.scanner.service.core.web

import akka.http.scaladsl.server.Route
import com.scanner.query.core.Response
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.Future
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._

/**
  * Created by Iurii on 08-03-2017.
  */
trait Api extends CirceSupport {

  def completeQuery(response: Future[Any])(happyPath: PartialFunction[Response, Route]): Route = {
    onComplete(response.mapTo[Response]){ tryResult =>
      tryResult.fold(
        _ => complete(InternalServerError),
        resp => happyPath(resp)
      )
    }
  }


}
