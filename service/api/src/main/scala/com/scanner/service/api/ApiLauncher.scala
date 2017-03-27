package com.scanner.service.api

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by IGorbylov on 21.03.2017.
  */
trait ApiLauncher extends App with ApiConfig {

  def log: Logger = Logger(LoggerFactory.getLogger(getClass))

  implicit val system = ActorSystem(akkaSystemName)
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10 seconds)

  def launch(): Unit = {

    for {
      wizzairService <- locateActor(wizzairHost, wizzairPort, wizzairName).resolveOne()
    } yield {
      log.debug(s"Starting $serviceName. Binding to port $httpPort")
      Http().bindAndHandle(routes(wizzairService), httpInterface, httpPort)
    }

  }

  def locateActor(host: String, port: String, name: String) = system.actorSelection(s"akka.tcp://story@$host:$port/user/$name")

  def routes(service: ActorRef): Route

}