package com.scanner.service.api

import akka.actor.{Actor, ActorLogging, ActorSelection}
import com.scanner.query.api._
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import com.scanner.service.api.ApiService.BuildGraph
import com.scanner.service.core.graphs.Graph

import scala.util.{Failure, Success}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.scanner.service.core.utils.SequenceUtils.FutureSequence
import com.scanner.service.core.utils.Exceptions.ExceptionUtils

/**
  * Created by IGorbylov on 04.04.2017.
  */
class ApiService(wizzairService: ActorSelection) extends Actor with ActorLogging{

  implicit val askTimeout = Timeout(10 seconds)

  var graph: Graph[String] = Graph.empty

  override def preStart(): Unit = self ! BuildGraph

  override def receive: Receive = {
    case BuildGraph =>
      List(wizzairService).map { service =>
        (service ? GetConnectionsQuery)
          .mapTo[GetConnectionsResponse]
          .map { response =>
            response.connections.toList.flatMap { case (origin, connections) => connections.map((origin, _))}
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

object ApiService{
  case object BuildGraph
}