package com.scanner.service.core.actor

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem}
import akka.testkit.TestActorRef

/**
  * Created by igorbylov on 13.07.17.
  */
trait CustomActorMocking {

  def mockActorRef(name: String)(mockedReceive: PartialFunction[Any, Unit])(implicit system: ActorSystem): ActorRef = {
    TestActorRef(
      new Actor {
        override def receive: Receive = mockedReceive
      },
      name
    )
  }

  def mockActorSelection(name: String)(mockedReceive: Receive)(implicit system: ActorSystem): ActorSelection = {
    val mockedActorRef = mockActorRef(name)(mockedReceive)
    ActorSelection(mockedActorRef, "/")
  }

}
