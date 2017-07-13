package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorSelection}
import com.scanner.message.api.{Airline, FlightView, GetFlightsMessage}
import com.scanner.message.core.Message
import com.scanner.service.core.actor.ActorService

/**
  * Created by igorbylov on 28.06.17.
  */
class FlightsAgregator(airlineServices: List[(Airline, ActorSelection)]) extends Actor
  with ActorLogging
  with ActorService {

  val flightsState: Map[String, Map[String, List[FlightView]]] = Map.empty

  override def handleMessage: Function[Message, Unit] = {
    case GetFlightsMessage(requestId, pathId, origin, arrival, from, to, airlines, currency) =>
      log.info("FlightsAggregator not implemented yet")
  }
}
