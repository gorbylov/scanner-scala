package com.scanner.service.api

import com.scanner.service.core.Config
import com.typesafe.config.ConfigFactory

/**
  * Created by IGorbylov on 21.03.2017.
  */
trait ApiConfig {
  val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val wiizairConfig = config.getConfig("scanner.wizzair")
  val wizzairHost = wiizairConfig.getString("host")
  val wizzairPort = wiizairConfig.getString("port")
  val wizzairName = wiizairConfig.getString("name")
}