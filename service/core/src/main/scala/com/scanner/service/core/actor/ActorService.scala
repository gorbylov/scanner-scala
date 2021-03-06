package com.scanner.service.core.actor

import akka.actor.{Actor, ActorLogging, ActorSelection}
import com.scanner.message.core.{Message, Response, TestMessage}
import com.typesafe.config.Config

/**
  * Created by igorbylov on 11.07.17.
  */
trait ActorService { this: Actor with ActorLogging =>

  override def receive(): PartialFunction[Any, Unit] = {
    case message: Message =>
      log.info(s"Received a message $message from ${sender().path.address.toString}")
      handleMessage(message)
    case response: Response =>
      log.info(s"Received a response $response from ${sender().path.address.toString}")
      handleResponse(response)
    case testMessage: TestMessage =>
      log.info(s"Received a test message $testMessage from ${sender().path.address.toString}")
      handleTestMessage(testMessage)
  }

  def handleMessage: Function[Message, Unit] = _ =>
    log.warning("Default handleMessage implementation called")

  def handleResponse: Function[Response, Unit] = _ =>
    log.warning("Default handleResponse implementation called")

  def handleTestMessage: Function[TestMessage, Unit] = _ =>
    log.warning("Default handleTestMessage implementation called")

  def locateRemoteActor(actorAddressConfig: Config): ActorSelection = {
    val host = actorAddressConfig.getString("host")
    val port = actorAddressConfig.getString("port")
    val name = actorAddressConfig.getString("name")
    context.actorSelection(s"akka.tcp://scanner@$host:$port/user/$name")
  }

}
