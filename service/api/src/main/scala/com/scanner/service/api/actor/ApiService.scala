package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.scanner.query.api.{Airline, OneWay, Wizzair}
import com.scanner.service.api.ApiApp.{locateActor, system, wizzairConfig}
import com.scanner.service.api.ApiConfig
import com.scanner.service.api.message.{BuildPath, CollectOneWayFlights, OneWayRequest}
import com.scanner.service.core.actor.ActorSelecting

import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence
import com.scanner.service.core.utils.Exceptions.ExceptionUtils

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService extends Actor with ActorLogging with ActorSelecting with ApiConfig {

  val airlineServices = List(
    Wizzair -> locateActor(wizzairConfig)
  )

  val flightsAgregatorService = context.actorOf(
    Props(classOf[FlightsAgregator], airlineServices),
    "flightsAgregator"
  )

  val pathService = context.actorOf(
    Props(classOf[PathService], flightsAgregatorService, airlineServices),
    "pathService"
  )

  override def receive: Receive = {
    case OneWayRequest(ctx, params) => pathService ! BuildPath(ctx, params, OneWay)



//    case oneWayQuery: GetOneWayFlightsRequest =>
//      val currentSender = sender()
//      oneWayQuery.airlines.map{
//        case Wizzair => (wizzairService ? oneWayQuery)
//          .mapTo[GetOneWayFlightsResponse]
//          .map(_.flights)
//      }
//        .toList.sequence
//        .map(flights => GetOneWayFlightsResponse(flights.flatten))
//        .pipeTo(currentSender)


  }
}

object ApiService{
  case object BuildGraph
}