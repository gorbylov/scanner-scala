package com.scanner.service.wizzair.service.impl

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.stream.Materializer
import akka.util.ByteString
import com.scanner.service.wizzair.protocol.{WizzairCities, WizzairTimetableResponse}
import com.scanner.service.wizzair.service.WizzairService
import io.circe.generic.auto._
import io.circe.parser._
import com.scanner.core.json.BasicCodecs._

import scala.concurrent.{ExecutionContext, Future}

class WizzairServiceImpl()(implicit ec: ExecutionContext, as: ActorSystem, m: Materializer) extends WizzairService {

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def fetchConnections(): Future[Map[String, List[String]]] = {
    for {
      apiUrl    <- fetchApiUrl() // TODO cache
      response  <- Http().singleRequest(HttpRequest(uri = s"$apiUrl/asset/map?languageCode=en-gb"))
      content   <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      json      <- Future.fromTry(parse(content).toTry)
      result    <- Future.fromTry(json.as[WizzairCities].toTry)
    } yield result.cities.map(city => city.iata -> city.connections.map(_.iata)).toMap
  }

  def fetchFlights(
    origin: String,
    arrival: String,
    startDate: LocalDate,
    endDate: LocalDate
  ): Future[WizzairTimetableResponse] = {
    val data = // TODO case class
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
      apiUrl    <- fetchApiUrl() // TODO cache
      response  <- Http().singleRequest(HttpRequest(uri = s"$apiUrl/search/timetable", method = POST, entity = HttpEntity(ContentTypes.`application/json`, data)))
      content   <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      json      <- Future.fromTry(parse(content).toTry)
      result    <- Future.fromTry(json.as[WizzairTimetableResponse].toTry)
    } yield result
  }

  def fetchApiUrl(): Future[String] = {
    for {
      response  <- Http().singleRequest(HttpRequest(uri = "https://wizzair.com/static/metadata.json"))
      content   <- response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      json      <- Future.fromTry(parse(content).toTry)
      result    <- Future.fromTry(json.hcursor.field("apiUrl").as[String].toTry)
    } yield result
  }

}