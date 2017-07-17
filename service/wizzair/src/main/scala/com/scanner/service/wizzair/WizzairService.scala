package com.scanner.service.wizzair

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.scanner.message.api._
import com.scanner.message.core.Message
import com.scanner.message.wizzair.{GetWizzairFlightsMessage, GetWizzairFlightsResponse, WizzairFailure, WizzairResponse}
import com.scanner.service.core.actor.ActorService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.scanner.service.core.utils.Dates._
import com.scanner.service.core.utils.SequenceUtils._

import scala.concurrent.Future
import scala.io.Source
import io.circe.generic.auto._
import io.circe.parser._

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService extends Actor
  with ActorLogging
  with ActorService {

  import WizzairService._

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    Props[WizzairWorker].withRouter(RoundRobinPool(5)),
    "wizzairRouter"
  )


  override def handleMessage: Function[Message, Unit] = {

    case GetConnectionsMessage =>
      val futureConnections = fetchWizzairConnections()
      futureConnections pipeTo sender()

    case GetFlightsMessage(stepIndex, stepsCount, origin, arrival, from, to, currency) =>
      val currentSender = sender()

      val futureResponses = (from, to).toMonthsInterval().map { date =>
        (wizzairRouter ? GetWizzairFlightsMessage(origin.iata, arrival.iata, date.getYear, date.getMonth.getValue))
          .mapTo[WizzairResponse]
      }.sequence

      futureResponses
        .map { responses =>
          responses.filter {
            case _: GetWizzairFlightsResponse => true
            case _: WizzairFailure => false
          }
            .flatMap {
              case GetWizzairFlightsResponse(flights) =>
                flights.map { view =>
                  FlightView(
                    view.flightNumber,
                    origin,
                    arrival,
                    view.departureTime,
                    view.arrivalTime,
                    Wizzair,
                    view.price,
                    view.currency
                  )
                }
            }
        }
        .map(flights => GetFlightsResponse(stepIndex, stepsCount, flights))
        .pipeTo(currentSender)
  }

  def fetchWizzairConnections(): Future[GetConnectionsResponse] = {
    val futureConnections = for {
      content <- Future(Source.fromURL(s"$apiRoot/asset/map?languageCode=en-gb", "UTF-8").mkString)
      json <- Future.fromTry(parse(content).toTry)
      connections <- Future.fromTry(json.as[WizzairCities].toTry)
    } yield connections

    futureConnections
      .map(cities => cities.cities.map(city => city.iata -> city.connections.map(_.iata)).toMap)
      .map(GetConnectionsResponse)
  }

}

object WizzairService {
  val apiVersion = "6.0.3" // TODO find out how to get api version
  val apiRoot = s"https://be.wizzair.com/$apiVersion/Api"

  case class WizzairCities(cities: List[WizzairCity])
  case class WizzairCity(
    iata: String,
    shortName: String,
    latitude: BigDecimal,
    longitude: BigDecimal,
    connections: List[WizzairConnection]
  )
  case class WizzairConnection(iata: String)

}
