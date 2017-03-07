package com.scanner.service.currency

import akka.actor.Props
import com.scanner.service.core.Launcher
import com.typesafe.scalalogging.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * Created by Iurii on 06-03-2017.
  */
object CurrencyApp extends Launcher with CurrencyConfig {

  launch

  override def log = Logger(LoggerFactory.getLogger(getClass))
  override def props: Props = Props(
    classOf[CurrencyService],
    system.scheduler
  )
}