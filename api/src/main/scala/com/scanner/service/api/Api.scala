package com.scanner.service.api

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport
import com.scanner.service.api.http.CustomDirectives.{requestParams, requestTimeout, tell, validate}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.core.json.BasicCodecs._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import com.scanner.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._
import com.scanner.service.api.message.RequestMessage

/**
  * Created by IGorbylov on 09.03.2017.
  */
trait Api extends CirceSupport {

  def routes(apiService: ActorRef): Route = encodeResponse {
    path("scan") {
      get {
        requestParams { params =>
          validate(params) {
            requestTimeout(10 seconds) {
              tell { ctx =>
                apiService ! RequestMessage(ctx, params)
              }
            }
          }
        }
      }
    }
  }


}