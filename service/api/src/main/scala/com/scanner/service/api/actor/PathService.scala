package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import com.scanner.message.api._
import com.scanner.service.core.graphs.Graph
import akka.pattern.ask
import akka.util.Timeout
import com.scanner.message.core.{Message, TestMessage}
import com.scanner.service.core.actor.ActorService

import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence
import com.scanner.service.core.utils.Exceptions.ExceptionUtils
import com.scanner.service.core.utils.MathUtils.haversineDistance

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by Iurii on 20-06-2017.
  */
class PathService(
  flightsAggregator: ActorRef,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor
  with ActorLogging
  with ActorService {

  var graph: Graph[Airport] = Graph.empty

  val emptyAirport = Airport("", "", 0, 0) // see AirportService to do

  implicit val timeout = Timeout(20 seconds) // TODO get rid of it


  override def handleMessage: Function[Message, Unit] = {

    case BuildPathMessage(requestId, origin, arrival, params) => {

      val paths = graph.search(origin, arrival) {
        case (airport1, airport2) => haversineDistance(airport1.lat -> airport1.lon, airport2.lat -> airport2.lon)
      }

      val flightsFetcherPerRequest = context.actorOf(
        Props(classOf[FlightsFetcherPerRequest], requestId, paths.size, flightsAggregator, airlineServices),
        s"flightsFetcherPerRequest-$requestId"
      )

      paths.foreach{ path =>
        val pathByPairs = path zip path.tail
        flightsFetcherPerRequest ! FetchFlightsForPathMessage(
          pathByPairs,
          params.from,
          params.to,
          params.airlines,
          params.currency,
          params.direction
        )
      }
    }

    case BuildGraphMessage(airportsState) =>
      airlineServices.map {
        case (_, service) =>
          (service ? GetConnectionsMessage)
            .mapTo[GetConnectionsResponse]
            .map { response =>
              response.connections.toList.flatMap { case (origin, connections) => connections.map((origin, _)) }
            }
      }
        .sequence
        .map(_.flatten)
        .onComplete{
          case Success(relations) =>
            val airportRelations = relations.map{
              case (code1, code2) =>
                airportsState.getOrElse(code1, emptyAirport) -> airportsState.getOrElse(code2, emptyAirport)
            }
            graph = Graph.build(airportRelations, 2)
          case Failure(error) =>
            log.error(s"An error occurred while building a graph.\n${error.mkString()}")
        }
  }

  override def handleTestMessage: Function[TestMessage, Unit] = {
    case GraphIsEmptyMessage =>
      sender() ! GraphIsEmptyResponse(graph.isEmpty())
  }
}
