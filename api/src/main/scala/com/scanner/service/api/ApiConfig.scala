package com.scanner.service.api

import com.typesafe.config.ConfigFactory

/**
  * Created by IGorbylov on 21.03.2017.
  */
trait ApiConfig {
  val apiConfig = ConfigFactory.load()
  val httpInterface = apiConfig.getString("http.interface")
  val httpPort = apiConfig.getInt("http.port")

  val systemName = apiConfig.getString("scanner.system.name")

  val wizzairConfig = apiConfig.getConfig("scanner.wizzair")
}