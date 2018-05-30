package com.scanner.service.wizzair.protocol

import java.time.LocalDateTime

case class WizzairCities(cities: List[WizzairCity])

case class WizzairCity(
  iata: String,
  shortName: String,
  latitude: BigDecimal,
  longitude: BigDecimal,
  connections: List[WizzairConnection]
)

case class WizzairConnection(iata: String)

case class WizzairTimetableResponse(
  outboundFlights: List[WizzairFlightInfoDto],
  returnFlights: List[WizzairFlightInfoDto]
)

case class WizzairFlightInfoDto(
  departureStation: String,
  arrivalStation: String,
  departureDate: LocalDateTime,
  price: WizzairFlightPriceDto,
  priceType: String,
  departureDates: List[LocalDateTime],
  classOfService: String,
  hasMacFlight: Boolean
)

case class WizzairFlightPriceDto(
  amount: Int,
  currencyCode: String
)
