package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging}
import com.scanner.message.api.AggregateFlights

/**
  * Created by igorbylov on 13.07.17.
  */
class FlightsAggregator extends Actor with ActorLogging {

  override def receive: Receive = {
    case AggregateFlights(requestId, flights) =>
      log.error("FlightsAggregator is not implemented yet.")
  }
}
