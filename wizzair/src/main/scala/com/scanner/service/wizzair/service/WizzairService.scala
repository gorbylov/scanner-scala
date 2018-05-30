package com.scanner.service.wizzair.service

import java.time.LocalDate

import com.scanner.service.wizzair.protocol.WizzairTimetableResponse

import scala.concurrent.Future

trait WizzairService {

  def fetchConnections(): Future[Map[String, List[String]]]

  def fetchFlights(
    origin: String,
    arrival: String,
    startDate: LocalDate,
    endDate: LocalDate
  ): Future[WizzairTimetableResponse]

}
