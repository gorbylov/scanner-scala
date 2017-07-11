package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.scanner.query.api.{ResolveAirportMessage, Wizzair}
import com.scanner.service.api.ApiConfig
import com.scanner.service.api.http.ImperativeRequestContext
import com.scanner.service.api.message.RequestMessage
import com.scanner.service.core.actor.ActorSelecting

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService extends Actor with ActorLogging with ActorSelecting with ApiConfig {

  var requestsState: Map[String, ImperativeRequestContext] = Map.empty

  val airlineServices = List(
    Wizzair -> locateRemoteActor(wizzairConfig)
  )

  val flightsAgregatorService = context.actorOf(
    Props(classOf[FlightsAgregator], airlineServices),
    "flightsAgregator"
  )

  val pathService = context.actorOf(
    Props(classOf[PathService], flightsAgregatorService, airlineServices),
    "pathService"
  )

  val airportService = context.actorOf(
    Props(classOf[AirportService], pathService),
    "airportService"
  )

  override def receive: Receive = {
    case RequestMessage(ctx, params) =>
      val requestId = UUID.randomUUID().toString
      requestsState = requestsState + (requestId -> ctx)
      airportService ! ResolveAirportMessage(requestId, params)
  }
}