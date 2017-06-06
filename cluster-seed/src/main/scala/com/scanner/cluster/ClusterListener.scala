package com.scanner.cluster

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * Created by Iurii on 07-06-2017.
  */
class ClusterListener extends Actor with ActorLogging{

  private val members = scala.collection.mutable.Set.empty[Address]

  Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberEvent])

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"Member joinerd ${member.address}")
      members += member.address

    case MemberUp(member) =>
      log.info(s"Member up: ${member.address}")
      members += member.address

    case MemberRemoved(member, _) =>
      log.info(s"Member removed: ${member.address}")
      members -= member.address
  }
}
