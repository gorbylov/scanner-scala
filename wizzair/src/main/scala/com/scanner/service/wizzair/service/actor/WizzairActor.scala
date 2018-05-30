package com.scanner.service.wizzair.service.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.scanner.core.utils.Dates._
import com.scanner.core.utils.SequenceUtils._
import com.scanner.protocol.api._
import com.scanner.protocol.wizzair.{GetWizzairFlightsMessage, GetWizzairFlightsResponse, WizzairFailure, WizzairResponse}
import com.scanner.service.wizzair.service.WizzairService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WizzairActor(
  wizzairService: WizzairService
)(implicit ec: ExecutionContext) extends Actor
  with ActorLogging {

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    WizzairActorWorker.props(wizzairService).withRouter(RoundRobinPool(5)), // TODO move routers count to config
    "wizzairRouter"
  )


  override def receive: Receive = {
    case GetConnectionsMessage =>
      wizzairService.fetchConnections() pipeTo sender()

    case GetFlightsMessage(stepIndex, stepsCount, origin, arrival, from, to, currency) =>
      val futureResponses = (from, to).toMonthsInterval().map { date =>
        (wizzairRouter ? GetWizzairFlightsMessage(origin.iata, arrival.iata, date)).mapTo[WizzairResponse]
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
        .pipeTo(sender())
  }
}

object WizzairActor {
  def props(wizzairService: WizzairService)(implicit ec: ExecutionContext, as: ActorSystem, m: Materializer): Props = {
    Props(new WizzairActor(wizzairService))
  }
}
