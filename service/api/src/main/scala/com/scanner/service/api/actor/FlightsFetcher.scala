package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection}
import com.scanner.message.api._
import com.scanner.message.core.{Message, Response, TestMessage}
import com.scanner.service.core.actor.ActorService

/**
  * Created by igorbylov on 28.06.17.
  */
class FlightsFetcher(
  flightsAggregator: ActorRef,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor
  with ActorLogging
  with ActorService {

  val flightsState: Map[Int, List[FlightView]] = Map.empty

  override def handleMessage: Function[Message, Unit] = {
    case FetchFlightsForPathMessage(requestId, path, from, to, airlines, currency, OneWay) => {
      val stepsWithIndexes = path zipWithIndex

      airlineServices.foreach { case (airlineName, airlineService) =>
        stepsWithIndexes.foreach { case ((origin, arrival), stepIndex) =>
          airlineService ! GetFlightsMessage(stepIndex, stepsWithIndexes.size, origin, arrival, from, to, currency)
        }
      }

//      TODO WTF???
//      for {
//        (airlineName, airlineService) <- airlineServices
//        ((origin, arrival), stepIndex) <- stepsWithIndexes
//      } yield airlineService ! GetFlightsMessage(stepIndex, stepsWithIndexes.size, origin, arrival, from, to, currency)

    }


    case FetchFlightsForPathMessage(requestId, path, from, to, airlines, currency, BothWays) =>
      log.info("FlightsAggregator.GetFlightsMessage not implemented yet")
  }

  override def handleResponse: Function[Response, Unit] = {
    case GetFlightsResponse(stepIndex, stepsCount, flights) => {
      val resultFlights = flightsState.get(stepIndex).fold(flights){existedFlights =>
        existedFlights ::: flights
      }
      flightsState + (stepIndex -> resultFlights)
      if (flightsState.keys.size == stepsCount) {
        // TODO send flights to aggregator
        context.stop(self)
      }
    }
  }

  override def handleTestMessage: Function[TestMessage, Unit] = {
    case GetFlightsStateMessage =>
      sender() ! flightsState
  }
}
