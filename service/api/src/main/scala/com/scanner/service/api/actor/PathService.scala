package com.scanner.service.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection}
import com.scanner.query.api._
import com.scanner.service.core.graphs.Graph
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence
import com.scanner.service.core.utils.Exceptions.ExceptionUtils
import com.scanner.service.core.utils.MathUtils

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by Iurii on 20-06-2017.
  */
class PathService(
  flightsAgregator: ActorRef,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor with ActorLogging {

  var graph: Graph[Airport] = Graph.empty

  val emptyAirport = Airport("", "", 0, 0) // see AirportService to do

  implicit val timeout = Timeout(20 seconds) // TODO get rid of it

  override def receive: Receive = {

    case BuildPathMessage(requestId, origin, arrival, params) =>
      val connections = graph.search(origin, arrival) {
        case (a1, a2) => MathUtils.haversineDistance(a1.lat -> a1.lon, a2.lat -> a2.lon)
      }
      connections
        .map(path => (path zip path.tail, UUID.randomUUID().toString))
        .foreach{
          case (pathPairs, pathId) =>
            pathPairs.foreach{
              case (airport1, airport2) =>
                flightsAgregator ! GetFlightsMessage(
                  requestId,
                  pathId,
                  airport1,
                  airport2,
                  params.from,
                  params.to,
                  params.airlines,
                  params.currency
                )
            }
        }

    case BuildGraphMessage(airportsState) =>
      airlineServices.map {
        case (_, service) =>
          (service ? GetConnectionsQuery)
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

    case GraphIsEmptyQuery =>
      sender() ! GraphIsEmptyResponse(graph.isEmpty())





  }


}
