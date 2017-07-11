package com.scanner.service.api.marshal

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, MessageEntity}
import akka.util.ByteString
import com.scanner.message.api.FailureMessage
import io.circe.generic.auto._
import io.circe.syntax._

/**
  * Created by igorbylov on 28.06.17.
  */
object ApiMarshallers {

  implicit def failureMessageMarshaller: ToEntityMarshaller[FailureMessage] = {
    Marshaller.withFixedContentType[FailureMessage, MessageEntity](`application/json`) { fm =>
      HttpEntity(`application/json`, ByteString(fm.asJson.toString()))
    }
  }
}