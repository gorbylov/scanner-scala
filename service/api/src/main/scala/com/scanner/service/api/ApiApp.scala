package com.scanner.service.api

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.scanner.service.api.actor._
import com.typesafe.config.Config
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

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

  val apiService = system.actorOf(ApiService.props(), "apiService")

  Http().bindAndHandle(routes(apiService), httpInterface, httpPort)

  def locateRemoteActor(actorAddressConfig: Config): ActorSelection = {
    val host = actorAddressConfig.getString("host")
    val port = actorAddressConfig.getString("port")
    val name = actorAddressConfig.getString("name")
    system.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")
  }

}