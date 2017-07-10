package com.scanner.service.api.message

import com.scanner.query.api.RequestParams
import com.scanner.service.api.http.ImperativeRequestContext

/**
  * Created by igorbylov on 28.06.17.
  */
sealed trait ApiMessage

case class RequestMessage(
  context: ImperativeRequestContext,
  params: RequestParams
) extends ApiMessage