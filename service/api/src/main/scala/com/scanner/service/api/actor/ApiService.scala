package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scanner.query.api.{ResolveAirportMessage}
import com.scanner.service.api.ApiConfig
import com.scanner.service.api.http.ImperativeRequestContext
import com.scanner.service.api.message.RequestMessage

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService(airportService: ActorRef) extends Actor with ActorLogging with ApiConfig {

  var requestsState: Map[String, ImperativeRequestContext] = Map.empty

  override def receive: Receive = {
    case RequestMessage(ctx, params) =>
      val requestId = UUID.randomUUID().toString
      requestsState = requestsState + (requestId -> ctx)
      airportService ! ResolveAirportMessage(requestId, params)
  }
}