package com.scanner.service.wizzair

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.scanner.query.api.{GetOneWayFlightsQuery, GetOneWayFlightsResponse, GetOneWayFlightsView}
import com.scanner.query.wizzair.{GetWizzairFlightsQuery, GetWizzairFlightsResponse, WizzairFailure, WizzairResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.scanner.service.core.utils.Dates._
import com.scanner.service.core.utils.SequenceUtils._

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairService extends Actor {

  implicit val askTimeout = Timeout(10 seconds)
  val wizzairRouter: ActorRef = context.actorOf(
    Props[WizzairWorker].withRouter(RoundRobinPool(5)),
    "wizzairRouter"
  )

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, from, to, _, currency) =>
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
}
