package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scanner.query.api.Airport
import com.scanner.service.api.message.ResolveAirportMessage

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


  override def preStart(): Unit = {
    val airportsUrl = "https://raw.githubusercontent.com/jbrooksuk/JSON-Airports/master/airports.json"

    val futureAirports = for {
      content <- Future(Source.fromURL(airportsUrl).mkString)
      json <- Future.fromTry(parse(content).toTry)
      airports <- Future.fromTry(json.as[List[Airport]].toTry)
    } yield airports

    futureAirports.onComplete {
      case Success(airports) =>
        airportsState = airports.map(airport => airport.iata -> airport).toMap
      case Failure(error) =>
        log.error("An error occured while getting airports.", error)
    }
  }

  override def receive: Receive = {
    case ResolveAirportMessage(ctx, requestParams, direction) =>
      // TODO if origin or arrival airport is missed we need to try to find them on another service
      val originAirport = airportsState.getOrElse(requestParams.origin, Airport("", "", 0, 0))
      val arrivalAirport = airportsState.getOrElse(requestParams.arrival, Airport("", "", 0, 0))
      //pathService ! BuildPathMessage(originAirport, arrivalAirport, ctx, requestParams, direction)
  }

}
