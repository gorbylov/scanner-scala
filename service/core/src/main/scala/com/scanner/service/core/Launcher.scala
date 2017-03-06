package com.scanner.service.core

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.slf4j.Logger
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Iurii on 06-03-2017.
  */
trait Launcher extends App with Config {

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(10, TimeUnit.SECONDS)

  def launch = {
    log.info(s"Starting $serviceName service")
    val service = system.actorOf(
      props,
      serviceName
    )
  }

  def log: Logger

  def props: Props

}