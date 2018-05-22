package com.scanner.protocol.currency

import com.scanner.protocol.core.TestMessage

/**
  * Created by igorbylov on 11.07.17.
  */
sealed trait CurrencyTestMessage extends TestMessage

case object GetCurrencyStateMessage extends CurrencyTestMessage
case class GetCurrencyStateResponse(state: Map[String, BigDecimal]) extends CurrencyTestMessage