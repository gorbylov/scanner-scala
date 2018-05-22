package com.scanner.service.currency.model

import java.time.LocalDateTime

import com.scanner.service.currency.CurrencyConfig

case class CurrencyState(
  data: Map[String, BigDecimal],
  expiresAt: LocalDateTime = LocalDateTime.now().plusHours(6)
) {
  def nonEmpty: Boolean = data.nonEmpty

  def notExpired: Boolean = expiresAt.isAfter(LocalDateTime.now())
}

object CurrencyState extends CurrencyConfig {
  val empty = CurrencyState(Map.empty)
}