package com.scanner.message.currency

import com.scanner.message.core.TestMessage

/**
  * Created by igorbylov on 11.07.17.
  */
sealed trait CurrencyTestMessage extends TestMessage

case object GetCurrencyStateMessage extends CurrencyTestMessage
case class GetCurrencyStateResponse(state: Map[String, BigDecimal]) extends CurrencyTestMessage