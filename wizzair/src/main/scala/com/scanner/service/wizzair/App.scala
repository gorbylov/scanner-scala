package com.scanner.service.wizzair

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.scanner.service.wizzair.service.actor.WizzairActor
import com.scanner.service.wizzair.service.impl.WizzairServiceImpl

import scala.concurrent.ExecutionContext

object App extends App with WizzairConfig {

  implicit val as = ActorSystem(systemName)
  implicit val am = ActorMaterializer()
  implicit val ec = ExecutionContext.global

  val ref = as.actorOf(
    WizzairActor.props(new WizzairServiceImpl()),
    serviceName
  )
}
