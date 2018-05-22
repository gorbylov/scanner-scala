package com.scanner.service.wizzair.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.scanner.protocol.api._
import com.scanner.protocol.core.Message
import com.scanner.protocol.wizzair.{GetWizzairFlightsMessage, GetWizzairFlightsResponse, WizzairFailure, WizzairResponse}
import com.scanner.core.actor.ActorService
import com.scanner.core.utils.Dates._
import com.scanner.core.utils.Exceptions.ExceptionUtils
import com.scanner.core.utils.SequenceUtils._
import com.scanner.service.wizzair.utils.WizzairApiFetcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService(wizzairApiFetcher: WizzairApiFetcher) extends Actor
  with ActorLogging
  with ActorService {

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    WizzairWorker.props(wizzairApiFetcher).withRouter(RoundRobinPool(5)), // TODO move routers count to config
    "wizzairRouter"
  )


  override def handleMessage: Function[Message, Unit] = {

    case GetConnectionsMessage =>
      val futureConnections = wizzairApiFetcher.fetchConnections()
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
}

object WizzairService {
  def props(wizzairApiFetcher: WizzairApiFetcher): Props =
    Props(new WizzairService(wizzairApiFetcher))
}
