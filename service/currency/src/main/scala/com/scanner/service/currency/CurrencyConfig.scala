package com.scanner.service.currency

import com.typesafe.config.ConfigFactory

/**
  * Created by Iurii on 06-03-2017.
  */
trait CurrencyConfig {
  val currencyConfig = ConfigFactory.load()
  val systemName = currencyConfig.getString("scanner.system.name")
  val serviceName = currencyConfig.getString("scanner.system.name")
  val schedulerInterval = currencyConfig.getInt("scanner.scheduler.interval")
}