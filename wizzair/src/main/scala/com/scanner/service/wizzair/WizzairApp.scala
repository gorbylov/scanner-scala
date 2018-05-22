package com.scanner.service.wizzair

import akka.actor.ActorSystem
import com.scanner.service.wizzair.actor.WizzairService
import com.scanner.service.wizzair.utils.WizzairApiFetcher

object WizzairApp extends App with WizzairConfig {

  implicit val system = ActorSystem(systemName)

  val ref = system.actorOf(
    WizzairService.props(new WizzairApiFetcher),
    serviceName
  )
}
