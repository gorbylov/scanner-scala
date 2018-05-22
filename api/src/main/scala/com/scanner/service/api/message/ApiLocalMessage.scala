package com.scanner.service.api.message

import com.scanner.protocol.api.RequestParams
import com.scanner.protocol.core.Message
import com.scanner.service.api.http.ImperativeRequestContext

/**
  * Created by igorbylov on 28.06.17.
  */
sealed trait ApiLocalMessage extends Message

case class RequestMessage(
  context: ImperativeRequestContext,
  params: RequestParams
) extends ApiLocalMessage