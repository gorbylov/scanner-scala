package com.scanner.service.api

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
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

  locateActor(wizzairConfig.getString("host"), wizzairConfig.getString("port"), wizzairConfig.getString("name"))
    .resolveOne()
    .onComplete{
      case Success(wizzairService) =>
        val apiGuard = system.actorOf(
          Props(classOf[ApiGuard], wizzairService),
          "apiGuard"
        )
        Http().bindAndHandle(routes(apiGuard), httpInterface, httpPort)
      case Failure(error) =>
        log.error(s"${error.getMessage}\nWizzairService is unreachable.")
    }

  def locateActor(host: String, port: String, name: String): ActorSelection =
    system.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")

}