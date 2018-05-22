package com.scanner.protocol.currency

import com.scanner.protocol.core.{Message, Response}

/**
  * Created by Iurii on 06-03-2017.
  */
sealed trait CurrencyMessage extends Message
sealed trait CurrencyResponse extends Response

case class ConvertCurrencyMessage(
  from: String,
  to: String,
  value: BigDecimal
) extends CurrencyMessage
case class ConvertCurrencyResponse(value: Option[BigDecimal]) extends CurrencyResponse

case class GetCurrenciesCoefficientMessage(from: String, to: String) extends CurrencyMessage
case class GetCurrenciesCoefficientResponse(coefficient: Option[BigDecimal]) extends CurrencyResponse

case object UpdateCurrencyStateMessage extends Message