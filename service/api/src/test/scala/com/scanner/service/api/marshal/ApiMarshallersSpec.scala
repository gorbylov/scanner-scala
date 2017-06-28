package com.scanner.service.api.marshal

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.stream.ActorMaterializer
import com.scanner.service.api.Api.FailureMessage
import org.scalatest.{FunSuite, Matchers, WordSpec}
import com.scanner.service.api.marshal.ApiMarshallers.failureMessageMarshaller

import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.concurrent.duration._
import io.circe.parser._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by igorbylov on 28.06.17.
  */
class ApiMarshallersSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  "ApiMarshallers" should {

    "marshall FailureMessage to HttpEntity with application/json content type" in {
      val message = FailureMessage(500, "message")

      val futureFailureMessage = for {
        httpEntity <- Marshal(message).to[HttpEntity]
        content <- httpEntity.toStrict(5 seconds).map(_.data.utf8String)
        result <- Future.fromTry(parse(content).flatMap(_.as[FailureMessage]).toTry)
      } yield (httpEntity.contentType, result)

      futureFailureMessage onComplete {
        case Success((contentType, failureMessage)) =>
          contentType shouldBe `application/json`
          failureMessage shouldBe message
        case Failure(error) => fail(error)
      }
    }
  }

}
