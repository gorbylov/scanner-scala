package com.scanner.service.api.wizzair

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService extends Actor {
  implicit val askTimeout = Timeout(10 seconds)
  val worker = context.actorOf(Props(classOf[WizzairWorker]))

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) =>
      val currentSender = sender()
      (worker ? GetOneWayFlightsQuery(origin, arrival, start, end, Seq(), currency))
        .mapTo[GetOneWayFlightsResponse]
        .pipeTo(currentSender)
  }
}
