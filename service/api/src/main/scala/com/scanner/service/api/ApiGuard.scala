package com.scanner.service.api

import akka.actor.{Actor, ActorRef, Props}
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse, Wizzair}

import akka.util.Timeout
import akka.pattern.{ask, pipe}
import com.scanner.service.api.wizzair.WizzairService

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiGuard extends Actor {

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairService: ActorRef = context.actorOf(Props(classOf[WizzairService]))

  override def receive: Receive = {
    case oneWayQuery: GetOneWayFlightsQuery =>
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
