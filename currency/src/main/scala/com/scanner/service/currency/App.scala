package com.scanner.service.currency

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.scanner.service.currency.service.actor.CurrencyActor

import scala.concurrent.ExecutionContext

object App extends App with CurrencyConfig {

  implicit val as = ActorSystem(systemName)
  implicit val am = ActorMaterializer()
  implicit val ec = ExecutionContext.global

  as.actorOf(CurrencyActor.props(), serviceName)
}