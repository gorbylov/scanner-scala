package com.scanner.query.currency

import com.scanner.query.core.{Query, Response}

/**
  * Created by Iurii on 06-03-2017.
  */
sealed trait CurrencyQuery extends Query
sealed trait CurrencyResponse extends Response

case class ConvertCurrencyQuery(
  from: String,
  to: String,
  value: BigDecimal
) extends CurrencyQuery
case class ConvertCurrencyResponse(value: Option[BigDecimal]) extends CurrencyResponse

case class GetCurrenciesCoefficientQuery(from: String, to: String) extends CurrencyQuery
case class GetCurrenciesCoefficientResponse(coefficient: Option[BigDecimal]) extends CurrencyResponse

case object UpdateCurrencyStateQuery extends Query