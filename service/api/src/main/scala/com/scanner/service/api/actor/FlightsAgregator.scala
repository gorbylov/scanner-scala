package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorSelection}
import com.scanner.query.api.{Airline, GetOneWayFlightsRequest, Wizzair}

/**
  * Created by igorbylov on 28.06.17.
  */
class FlightsAgregator(airlineServices: List[(Airline, ActorSelection)]) extends Actor with ActorLogging {

  override def receive: Receive = {
    case oneWayQuery: GetOneWayFlightsRequest =>
      oneWayQuery.airlines.map{
        case Wizzair => (wizzairService ? oneWayQuery)
          .mapTo[GetOneWayFlightsResponse]
          .map(_.flights)
      }
        .toList.sequence
        .map(flights => GetOneWayFlightsResponse(flights.flatten))
        .pipeTo(currentSender)*/
  }
}
