package com.scanner.service.api.wizzair

import akka.actor.{Actor, Props}
import com.scanner.query.api.GetOneWayFlightsQuery

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService extends Actor {

  val worker = context.actorOf(Props(classOf[WizzairWorker]))

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) =>
      worker ! GetOneWayFlightsQuery(origin, arrival, start, end, Seq(), currency)
  }
}
