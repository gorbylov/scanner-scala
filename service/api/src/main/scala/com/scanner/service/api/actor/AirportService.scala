package com.scanner.service.api.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.scanner.message.api._
import com.scanner.service.api.actor.AirportService.AirportDto

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

  var airportsState: Map[String, AirportView] = Map.empty

  override def preStart(): Unit = self ! FetchAirportsMessage

  override def receive: Receive = {

    case ResolveAirportMessage(requestId, requestParams) =>
      // TODO if origin or arrival airport is missed we need to try to find them on another service
      val emptyAirport = AirportView("", "", 0, 0)
      val originAirport = airportsState.getOrElse(requestParams.origin, emptyAirport)
      val arrivalAirport = airportsState.getOrElse(requestParams.arrival, emptyAirport)
      pathService ! BuildPathMessage(requestId, originAirport, arrivalAirport, requestParams)

    case FetchAirportsMessage =>
      val airportsUrl = "https://raw.githubusercontent.com/jbrooksuk/JSON-Airports/master/airports.json"

      val futureAirports = for {
        content <- Future(Source.fromURL(airportsUrl, "UTF-8").mkString)
        json <- Future.fromTry(parse(content).toTry)
        airports <- Future.fromTry(json.as[List[AirportDto]].toTry)
      } yield airports

      futureAirports.map(dtos =>
        dtos
          .filter(dto => dto.name.isDefined && dto.lat.isDefined && dto.lon.isDefined)
          .map(dto => AirportView(dto.iata, dto.name.get, dto.lat.get, dto.lon.get))
      )
        .onComplete {
          case Success(airports) =>
            airportsState = airports.map(airport => airport.iata -> airport).toMap
            pathService ! BuildGraphMessage(airportsState)
          case Failure(error) =>
            log.error("An error occurred while getting airports.", error)
        }

    case GetAirportsStateMessage =>
      sender() ! GetAirportsStateResponse(airportsState)
  }

}

object AirportService {
  case class AirportDto(
    iata: String,
    name: Option[String],
    lat: Option[BigDecimal],
    lon: Option[BigDecimal]
  )
}
