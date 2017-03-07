package com.scanner.service.currency

import com.typesafe.config.ConfigFactory

/**
  * Created by Iurii on 06-03-2017.
  */
trait CurrencyConfig {
  private val config = ConfigFactory.load()
  val interval = config.getInt("scanner.scheduler.interval")
}