package com.scanner.service.currency

import akka.actor.{ActorSystem, Props}
import com.scanner.message.currency.UpdateCurrencyStateMessage
import com.scanner.service.currency.actor.CurrencyService
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by Iurii on 06-03-2017.
  */
object CurrencyApp extends App with CurrencyConfig {

  val log = Logger(LoggerFactory.getLogger(getClass))
  implicit val system = ActorSystem(systemName)

  log.info(s"Starting $serviceName service.")
  val currencyService = system.actorOf(CurrencyService.props(), serviceName)
  //system.scheduler.schedule(0 seconds, schedulerInterval, currencyService, UpdateCurrencyStateMessage)
}