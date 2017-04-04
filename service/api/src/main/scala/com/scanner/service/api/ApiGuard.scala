package com.scanner.service.api

import akka.actor.{Actor, ActorRef, Props}
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse, Wizzair}
import com.scanner.service.wizzair.WizzairService

import scala.concurrent.Future
import akka.util.Timeout
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiGuard extends Actor {

  implicit val askTimeout = Timeout(5 seconds)
  val wizzairService: ActorRef = context.actorOf(Props(classOf[WizzairService]))

  override def receive: Receive = {
    case oneWayQuery: GetOneWayFlightsQuery =>
      val futureFlights = oneWayQuery.airlines.map{
        case Wizzair => (wizzairService ? oneWayQuery)
          .mapTo[GetOneWayFlightsResponse]
          .map(_.flights)
      }
      Future.sequence(futureFlights)
        .map(flights => GetOneWayFlightsResponse(flights.flatten))
        .pipeTo(sender())
  }
}
