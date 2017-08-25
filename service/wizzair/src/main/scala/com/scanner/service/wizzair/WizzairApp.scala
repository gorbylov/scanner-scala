package com.scanner.service.wizzair

import akka.actor.ActorSystem
import com.scanner.service.wizzair.actor.WizzairService
import com.scanner.service.wizzair.utils.WizzairApiFetcher
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

object WizzairApp extends App with WizzairConfig {

  val log = Logger(LoggerFactory.getLogger(getClass))
  implicit val system = ActorSystem(systemName)

  log.info(s"Starting $serviceName service.")
  val ref = system.actorOf(
    WizzairService.props(new WizzairApiFetcher),
    serviceName
  )
}
