package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging}

/**
  * Created by igorbylov on 13.07.17.
  */
class FlightsAggregator extends Actor with ActorLogging {

  override def receive: Receive = {
    case _ =>
      log.error("FlightsAggregator is not implemented yet.")
  }
}
