package com.scanner.service.api

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scanner.query.api.Wizzair
import com.scanner.service.api.actor.{AirportService, ApiService, FlightsAgregator, PathService}
import com.typesafe.config.Config
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by IGorbylov on 21.03.2017.
  */
object ApiApp extends App with Api with ApiConfig{

  def log: Logger = Logger(LoggerFactory.getLogger(getClass))

  implicit val system = ActorSystem(systemName)
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10 seconds)

  val airlineServices = List(
    Wizzair -> locateRemoteActor(wizzairConfig)
  )

  airlineServices.head._2.resolveOne(5 seconds).onComplete{
    case Success(ref) =>
      log.info("oye")
    case Failure(error) =>
      log.info(error.getMessage)
  }

  val flightsAgregatorService = system.actorOf(
    Props(classOf[FlightsAgregator], airlineServices),
    "flightsAgregator"
  )

  val pathService = system.actorOf(
    Props(classOf[PathService], flightsAgregatorService, airlineServices),
    "pathService"
  )

  val airportService = system.actorOf(
    Props(classOf[AirportService], pathService),
    "airportService"
  )

  val apiService = system.actorOf(
    Props(classOf[ApiService], airportService),
    "apiService"
  )

  Http().bindAndHandle(routes(apiService), httpInterface, httpPort)

  def locateActor(host: String, port: String, name: String): ActorSelection =
    system.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")

  def locateRemoteActor(actorAddressConfig: Config): ActorSelection = {
    val host = actorAddressConfig.getString("host")
    val port = actorAddressConfig.getString("port")
    val name = actorAddressConfig.getString("name")
    system.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")
  }

}