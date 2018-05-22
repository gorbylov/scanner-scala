package com.scanner.service.currency.protocol


sealed trait CurrencyInitializationMessage

case object FetchCurrencies extends CurrencyInitializationMessage

case class CurrenciesFetched(data: Map[String, BigDecimal]) extends CurrencyInitializationMessage

case class CurrenciesFetchingFailed(message: String) extends CurrencyInitializationMessage
