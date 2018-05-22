package com.scanner.cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class ClusterSeed extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"Member is joined: ${member.address}")
    case MemberUp(member) =>
      log.info(s"Member is up: ${member.address}")
    case MemberRemoved(member, _) =>
      log.info(s"Member removed: ${member.address}")
    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: ${member.address}")
    case _: MemberEvent => // ignore
  }
}