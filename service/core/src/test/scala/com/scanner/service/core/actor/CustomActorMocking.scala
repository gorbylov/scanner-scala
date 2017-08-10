package com.scanner.service.core.actor

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import akka.testkit.TestProbe

/**
  * Created by igorbylov on 13.07.17.
  */
trait CustomActorMocking {

  class ForwardActor(forwardTo: ActorRef) extends Actor {
    override def receive: Receive = {
      case anyMsg => forwardTo forward anyMsg
    }
  }

  object ForwardActor {
    def props(forwardTo: ActorRef): Props = Props(new ForwardActor(forwardTo))
  }

  def mockActorSelection(name: String)(implicit system: ActorSystem): (ActorSelection, TestProbe) = {
    val probe = TestProbe()
    system.actorOf(ForwardActor.props(probe.ref), name)
    (ActorSelection(probe.ref, "/"), probe)
  }
}
