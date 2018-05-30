package com.scanner.service.currency.service

import com.scanner.service.currency.model.Currencies

import scala.concurrent.Future

trait CurrencyService {

  /**
    * Fetches currency rates from 3rd party service
    * @return
    */
  def fetch(): Future[Currencies]

}
