package com.scanner.message.wizzair

import java.time.LocalDateTime

/**
  * Created by igorbylov on 11.07.17.
  */
trait WizzairView

case class WizzairFlightView(
  flightNumber: String,
  origin: String,
  arrival: String,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  price: BigDecimal,
  currency: String
)
