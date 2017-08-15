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
import com.scanner.service.core.utils.Exceptions.ExceptionUtils
import com.scanner.service.core.utils.SequenceUtils._

import scala.concurrent.Future
import scala.io.Source
import io.circe.generic.auto._
import io.circe.parser._

import scala.util.{Failure, Success}

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService extends Actor
  with ActorLogging
  with ActorService {

  import WizzairService._

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    WizzairWorker.props().withRouter(RoundRobinPool(5)), // TODO move routers count to config
    "wizzairRouter"
  )


  override def handleMessage: Function[Message, Unit] = {

    case GetConnectionsMessage =>
      val futureConnections = fetchWizzairConnections()
      futureConnections pipeTo sender()

    case GetFlightsMessage(stepIndex, stepsCount, origin, arrival, from, to, currency) =>
      val currentSender = sender()

      val futureResponses = (from, to).toMonthsInterval().map { date =>
        (wizzairRouter ? GetWizzairFlightsMessage(origin.iata, arrival.iata, date))
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
        .onComplete{
          case Success(response) =>
            currentSender ! response
          case Failure(error) =>
            log.error(error.mkString())
        }
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

  def props(): Props = Props(new WizzairService())

  val apiVersion = "6.3.0" // TODO find out how to get api version https://wizzair.com/static/metadata.json
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
