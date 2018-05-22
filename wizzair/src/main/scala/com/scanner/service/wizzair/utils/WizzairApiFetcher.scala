package com.scanner.service.wizzair.utils

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import com.scanner.service.wizzair.utils.WizzairApiFetcher.{WizzairCities, WizzairTimetableResponse, apiRoot}
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.service.wizzair.json.WizzairCodecs._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scalaj.http.Http

/**
  * Created by Iurii on 25-08-2017.
  */
class WizzairApiFetcher {

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def fetchConnections(): Future[Map[String, List[String]]] = {
    val futureResponse = for {
      content <- Future(Source.fromURL(s"$apiRoot/asset/map?languageCode=en-gb", "UTF-8").mkString)
      json <- Future.fromTry(parse(content).toTry)
      response <- Future.fromTry(json.as[WizzairCities].toTry)
    } yield response
    futureResponse
      .map(_.cities.map(city => city.iata -> city.connections.map(_.iata)).toMap)
  }

  def fetchTimetableFlights(
    origin: String,
    arrival: String,
    startDate: LocalDate,
    endDate: LocalDate): Future[WizzairTimetableResponse] = {
    val data =
      s"""{
         |  "flightList": [
         |    {
         |      "departureStation": "$origin",
         |      "arrivalStation": "$arrival",
         |      "from": "${startDate.format(formatter)}",
         |      "to": "${endDate.format(formatter)}"
         |    },
         |    {
         |      "departureStation": "$arrival",
         |      "arrivalStation": "$origin",
         |      "from": "${startDate.format(formatter)}",
         |      "to": "${endDate.format(formatter)}"
         |    }
         |  ],
         |  "priceType": "regular"
         |}
    """.stripMargin
    for {
      content <- Future(
        Http(s"$apiRoot/search/timetable")
          .method("POST")
          .postData(data)
          .header("Content-Type", "application/json")
          .asString.body
      )
      json <- Future.fromTry(parse(content).toTry)
      response <- Future.fromTry(json.as[WizzairTimetableResponse].toTry)
    } yield response
  }
}

object WizzairApiFetcher {

  val apiVersion = "6.3.0" // TODO find out how to get api version https://wizzair.com/static/metadata.json
  val apiRoot = s"https://be.wizzair.com/$apiVersion/Api"

  // connections dtos
  case class WizzairCities(cities: List[WizzairCity])

  case class WizzairCity(
    iata: String,
    shortName: String,
    latitude: BigDecimal,
    longitude: BigDecimal,
    connections: List[WizzairConnection]
  )

  case class WizzairConnection(iata: String)

  // flights dtos
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
}