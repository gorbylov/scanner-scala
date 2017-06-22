package com.scanner.service.api

import akka.actor.{Actor, ActorLogging, ActorSelection}
import akka.http.scaladsl.server.RequestContext
import com.scanner.query.api.{Airline, GetOneWayFlightsRequest, Wizzair}

import akka.pattern.ask

/**
  * Created by Iurii on 20-06-2017.
  */
class OneWayRequestHandler(
  requestContext: RequestContext,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor with ActorLogging {

  override def receive: Receive = {
    case oneWayQuery: GetOneWayFlightsRequest =>
      airlineServices
        .map {
          case (Wizzair, wizzairService) => (wizzairService ? oneWayQuery)
        }
  }
}
