package com.scanner.service.wizzair

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.scanner.query.api._
import com.scanner.query.wizzair.{GetWizzairFlightsQuery, GetWizzairFlightsResponse, WizzairFailure, WizzairResponse}

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
class WizzairService extends Actor {
  import WizzairService._

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    Props[WizzairWorker].withRouter(RoundRobinPool(5)),
    "wizzairRouter"
  )

  override def receive: Receive = {
    case GetConnectionsQuery => makeConnectionsResponse().pipeTo(sender())

    case GetOneWayFlightsRequest(origin, arrival, from, to, _, currency) =>
      val currentSender = sender()

      val futureResponses = (from, to).toMonthsInterval().map { date =>
        (wizzairRouter ? GetWizzairFlightsQuery(origin, arrival, date.getYear, date.getMonth.getValue))
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
                  GetOneWayFlightsView(
                    view.flightNumber,
                    view.origin,
                    view.arrival,
                    view.departureTime,
                    view.arrivalTime,
                    "wizzair",
                    view.price,
                    view.currency
                  )
                }
            }
        }
        .map(GetOneWayFlightsResponse)
        .pipeTo(currentSender)
  }

  def makeConnectionsResponse(): Future[GetConnectionsResponse] = {
    Future(Source.fromURL(s"$apiRoot/asset/map?languageCode=en-gb", "UTF-8").mkString)
      .flatMap(content =>
        Future.fromTry(parse(content).flatMap(json => json.as[WizzairCities]).toTry)
      )
      .map(cities => cities.cities.map(city => city.iata -> city.connections.map(_.iata)).toMap)
      .map(GetConnectionsResponse)
  }

}

object WizzairService {
  def apiRoot = s"https://be.wizzair.com/$apiVersion/Api"
  val apiVersion = "5.2.2"

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
