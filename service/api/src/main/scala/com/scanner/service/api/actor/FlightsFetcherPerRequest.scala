package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.scanner.message.api._
import com.scanner.message.core.{Message, Response, TestMessage}
import com.scanner.service.core.actor.ActorService

/**
  * Created by igorbylov on 28.06.17.
  */
class FlightsFetcherPerRequest(
  requestId: String,
  pathsCount: Int, // TODO try to get rid of requestId and pathCount from constructor
  flightsAggregator: ActorRef,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor
  with ActorLogging
  with ActorService {

  var flightsState: Map[Int, List[FlightView]] = Map.empty

  override def handleMessage: Function[Message, Unit] = {
    case FetchFlightsForPathMessage(path, from, to, airlines, currency, OneWay) => {
      val stepsWithIndexes = path.zipWithIndex
      airlineServices.foreach { case (_, airlineService) =>
        stepsWithIndexes.foreach { case ((origin, arrival), stepIndex) =>
          airlineService ! GetFlightsMessage(stepIndex, stepsWithIndexes.size, origin, arrival, from, to, currency)
        }
      }
    }


    case FetchFlightsForPathMessage(path, from, to, airlines, currency, BothWays) =>
      log.info("FlightsAggregator.GetFlightsMessage not implemented yet")
  }

  override def handleResponse: Function[Response, Unit] = {
    case GetFlightsResponse(stepIndex, stepsCount, flights) =>
      val resultFlights = flightsState.get(stepIndex).fold(flights){existedFlights =>
        existedFlights ::: flights
      }
      flightsState = flightsState + (stepIndex -> resultFlights)
      if (flightsState.keys.size == stepsCount) {
        val steppedFlights = flightsState.toList
          .sortBy { case (idx, _) => idx }
          .map{ case (_, f) => f}
        flightsAggregator ! AggregateFlights(requestId, pathsCount, steppedFlights)
        context.stop(self)
      }
  }

  override def handleTestMessage: Function[TestMessage, Unit] = {
    case GetFlightsStateMessage =>
      sender() ! GetFlightsStateResponse(flightsState)
  }
}

object FlightsFetcherPerRequest{
  def props(
    requestId: String,
    pathsCount: Int,
    flightsAggregator: ActorRef,
    airlineServices: List[(Airline, ActorSelection)]
  ): Props =
    Props(new FlightsFetcherPerRequest(requestId, pathsCount, flightsAggregator, airlineServices))
}
