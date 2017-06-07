package com.scanner.service.wizzair

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * Created by Iurii on 07-06-2017.
  */
object WizzairApp extends App with WizzairConfig {

  val log = Logger(LoggerFactory.getLogger(getClass))
  implicit val system = ActorSystem(systemName)

  log.info(s"Starting $serviceName service.")
  val ref = system.actorOf(
    Props(classOf[WizzairService]),
    serviceName
  )
  log.info(ref.path.name)
}
