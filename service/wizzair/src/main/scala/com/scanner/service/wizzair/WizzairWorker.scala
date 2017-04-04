package com.scanner.service.wizzair

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.Actor
import com.scanner.query.api.GetOneWayFlightsQuery
import com.scanner.service.wizzair.WizzairWorker.{API_URL, CONN_TIMEOUT, READ_TIMEOUT}
import io.circe.generic.auto._

import scalaj.http.Http

/**
  * Created by IGorbylov on 04.04.2017.
  */
class WizzairWorker extends Actor {

  override def receive: Receive = {
    case GetOneWayFlightsQuery(origin, arrival, start, end, _, currency) => sender ! flights(oneWayJson(origin, arrival, start, end))

  }

  private def flights(json: String) = {
    val result = Http(API_URL).postData(json)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .timeout(CONN_TIMEOUT, READ_TIMEOUT)
      .asString
    println(result)
  }

  private def oneWayJson(origin: String, arrival: String, start: LocalDate, end: LocalDate) = {
    s"""
       |{
       |  "flightList":[{
       |    "departureStation":"$origin",
       |    "arrivalStation":"$arrival",
       |    "from": "${start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
       |    "to": "${end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
       |   }]
       |}
      """.stripMargin
  }
}

object WizzairWorker {
  val API_URL = "https://be.wizzair.com/4.3.0/Api/search/timetable"
  val CONN_TIMEOUT = 10000
  val READ_TIMEOUT = 10000
}
