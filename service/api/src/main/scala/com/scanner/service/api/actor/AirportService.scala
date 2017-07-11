package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scanner.query.api._

import scala.concurrent.Future
import scala.io.Source
import io.circe.generic.auto._
import io.circe.parser._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by igorbylov on 07.07.17.
  */
class AirportService(pathService: ActorRef) extends Actor with ActorLogging {

  var airportsState: Map[String, Airport] = Map.empty

  override def preStart(): Unit = self ! FetchAirportsMessage

  override def receive: Receive = {
    case FetchAirportsMessage =>
      val airportsUrl = "https://raw.githubusercontent.com/jbrooksuk/JSON-Airports/master/airports.json"

      val futureAirports = for {
        content <- Future(Source.fromURL(airportsUrl).mkString)
        json <- Future.fromTry(parse(content).toTry)
        airports <- Future.fromTry(json.as[List[Airport]].toTry)
      } yield airports

      futureAirports.onComplete {
        case Success(airports) =>
          airportsState = airports.map(airport => airport.iata -> airport).toMap
          pathService ! BuildGraph(airportsState)
        case Failure(error) =>
          log.error("An error occurred while getting airports.", error)
      }

    case ResolveAirportMessage(requestId, requestParams) =>
      // TODO if origin or arrival airport is missed we need to try to find them on another service
      val emptyAirport = Airport("", "", 0, 0)
      val originAirport = airportsState.getOrElse(requestParams.origin, emptyAirport)
      val arrivalAirport = airportsState.getOrElse(requestParams.arrival, emptyAirport)
      pathService ! BuildPathMessage(requestId, originAirport, arrivalAirport, requestParams)

    case GetAirportsStateQuery =>
      sender() ! GetAirportsStateResponse(airportsState)
  }

}
