package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection}
import com.scanner.query.api._
import com.scanner.service.api.actor.ApiService.BuildGraph
import com.scanner.service.api.message.{BuildPath, CollectOneWayFlights}
import com.scanner.service.core.graphs.Graph
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence
import com.scanner.service.core.utils.Exceptions.ExceptionUtils

import scala.util.{Failure, Success}

/**
  * Created by Iurii on 20-06-2017.
  */
class PathService(
  flightsAgregator: ActorRef,
  airlineServices: List[(Airline, ActorSelection)]
) extends Actor with ActorLogging {

  var graph: Graph[String] = Graph.empty

  override def preStart(): Unit = self ! BuildGraph

  override def receive: Receive = {

    case BuildGraph =>
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
            graph = Graph.build(relations)
          case Failure(error) =>
            log.error(s"An error occurred while building a graph.\n${error.mkString()}")
        }


    case BuildPath(ctx, params, OneWay) =>
      // TODO origin as airport
     graph.search(params.origin, params.arrival) { (a, b) =>

     }

  }
}
