package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.protocol.api.{RequestResponse, ResolveAirportMessage, Wizzair}
import com.scanner.protocol.core.{Message, Response}
import com.scanner.service.api.ApiConfig
import com.scanner.service.api.http.ImperativeRequestContext
import com.scanner.service.api.message.RequestMessage
import com.scanner.core.actor.ActorService

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import com.scanner.core.json.BasicCodecs._
import com.scanner.core.marshal.BasicUnmarshallers._
import com.scanner.service.api.marshal.ApiUnmarshallers._

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService extends Actor
  with ActorLogging
  with ActorService
  with ApiConfig {

  val airlineServices = List(Wizzair -> locateRemoteActor(wizzairConfig))
  val flightsAggregator = context.actorOf(FlightsAggregator.props(self), "flightsAggregator")
  val pathService = context.actorOf(PathService.props(flightsAggregator, airlineServices), "pathService")
  val airportService = context.actorOf(AirportService.props(pathService), "airportService")

  var requestsState: Map[String, ImperativeRequestContext] = Map.empty

  override def handleMessage: Function[Message, Unit] = {
    case RequestMessage(ctx, params) =>
      val requestId = UUID.randomUUID().toString
      requestsState = requestsState + (requestId -> ctx)
      airportService ! ResolveAirportMessage(requestId, params)
  }

  override def handleResponse: Function[Response, Unit] = {
    case RequestResponse(requestId, flights) =>
      requestsState.get(requestId).foreach { ctx =>
        requestsState = requestsState - requestId
        ctx.complete(flights.mkString) // TODO
      }
  }
}

object ApiService {
  def props(): Props = Props(new ApiService())
}