package com.scanner.cluster

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.scanner.message.cluster.{GetClusterMembersMessage, GetClusterMembersResponse}

/**
  * Created by Iurii on 07-06-2017.
  */
class ClusterSeed extends Actor with ActorLogging{

  val cluster = Cluster(context.system)
  val members = scala.collection.mutable.Set.empty[String]

  override def preStart(): Unit =
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info(s"Member is up: ${member.address}")
      members += member.address.toString
    case MemberJoined(member) =>
      log.info(s"Member is joined: ${member.address}")
      members += member.address.toString
    case MemberRemoved(member, _) =>
      log.info(s"Member removed: ${member.address}")
      members -= member.address.toString
    case UnreachableMember(member) =>
      log.info(s"Member detected as unreachable: ${member.address}")
      members -= member.address.toString

    case GetClusterMembersMessage =>
      sender() ! GetClusterMembersResponse(members)
  }
}