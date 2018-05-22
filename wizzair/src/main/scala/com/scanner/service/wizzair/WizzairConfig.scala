package com.scanner.service.wizzair

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Iurii on 07-06-2017.
  */
trait WizzairConfig {
  val wizzairConfig = ConfigFactory.load()
  val systemName = wizzairConfig.getString("scanner.system.name")
  val serviceName = wizzairConfig.getString("scanner.service.name")

  val currencyConfig = wizzairConfig.getConfig("scanner.currency")
}
