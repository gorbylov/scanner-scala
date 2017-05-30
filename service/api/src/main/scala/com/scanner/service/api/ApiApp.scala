package com.scanner.service.api

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by IGorbylov on 21.03.2017.
  */
object ApiApp extends App with Api with ApiConfig{

  def log: Logger = Logger(LoggerFactory.getLogger(getClass))

  implicit val system = ActorSystem("temp")
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(10 seconds)

  def launch(): Unit = {
    val apiGuard = system.actorOf(Props(classOf[ApiGuard]))
    Http().bindAndHandle(routes(apiGuard), httpInterface, httpPort)
  }

  launch()

}