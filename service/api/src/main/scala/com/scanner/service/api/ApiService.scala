package com.scanner.service.api

import akka.actor.{Actor, ActorSelection}
import com.scanner.query.api.{GetOneWayFlightsRequest, GetOneWayFlightsResponse, Wizzair}
import akka.util.Timeout
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService(wizzairService: ActorSelection) extends Actor {

  implicit val askTimeout = Timeout(10 seconds)

  override def receive: Receive = {
    case oneWayQuery: GetOneWayFlightsRequest =>
      val currentSender = sender()
      oneWayQuery.airlines.map{
        case Wizzair => (wizzairService ? oneWayQuery)
          .mapTo[GetOneWayFlightsResponse]
          .map(_.flights)
      }
        .toList.sequence
        .map(flights => GetOneWayFlightsResponse(flights.flatten))
        .pipeTo(currentSender)
  }
}