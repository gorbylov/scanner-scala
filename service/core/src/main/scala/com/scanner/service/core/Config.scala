package com.scanner.service.core

import com.typesafe.config.ConfigFactory

/**
  * Created by Iurii on 06-03-2017.
  */
trait Config {
  val config = ConfigFactory.load()
  val serviceName = config.getString("scanner.module.name")
}