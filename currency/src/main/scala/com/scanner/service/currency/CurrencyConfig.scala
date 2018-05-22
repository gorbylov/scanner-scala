package com.scanner.service.currency

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{FiniteDuration, HOURS, SECONDS}

trait CurrencyConfig {

  val currencyConfig = ConfigFactory.load()

  val systemName = currencyConfig.getString("scanner.system.name")

  val serviceName = currencyConfig.getString("scanner.service.name")

  val fetchingInterval = FiniteDuration(currencyConfig.getDuration("scanner.fetchingInterval").toHours, HOURS)

  val failedFetchingInterval = FiniteDuration(currencyConfig.getDuration("scanner.failedFetchingInterval").getSeconds, SECONDS)

  val apilayerKey = currencyConfig.getString("scanner.apilayer.key")
}