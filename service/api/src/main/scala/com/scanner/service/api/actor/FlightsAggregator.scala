package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scanner.message.api._
import com.scanner.message.core.{Message, TestMessage}
import com.scanner.service.core.actor.ActorService

/**
  * Created by igorbylov on 13.07.17.
  */
class FlightsAggregator(apiService: ActorRef) extends Actor
  with ActorLogging
  with ActorService {

  type RequestId = String
  type Path = List[List[FlightView]]

  var state : Map[RequestId, List[Path]] = Map.empty // TODO timeout

  override def handleMessage: Function[Message, Unit] = {
    case AggregateFlights(requestId, pathsCount, flights) =>
      val newPaths = state.get(requestId).fold(flights :: Nil){ existedPaths =>
        flights :: existedPaths
      }

      newPaths match {
        case it if it.size == pathsCount =>
          apiService ! RequestResponse(requestId, newPaths.flatten)
          state = state - requestId
        case _ =>
          state = state + (requestId -> newPaths)
      }
  }

  override def handleTestMessage: Function[TestMessage, Unit] = {
    case GetFlightsAggregatorStateMessage =>
      sender() ! GetFlightsAggregatorStateResponse(state)
  }
}
