package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorSelection}
import com.scanner.message.api.{Airline, FlightView, GetFlightsMessage}

/**
  * Created by igorbylov on 28.06.17.
  */
class FlightsAgregator(airlineServices: List[(Airline, ActorSelection)]) extends Actor with ActorLogging {

  val flightsState: Map[String, Map[String, List[FlightView]]] = Map.empty

  override def receive: Receive = {
    case GetFlightsMessage(requestId, pathId, origin, arrival, from, to, airlines, currency) =>
      log.info("FlightsAggregator not implemented yet")
  }
}
