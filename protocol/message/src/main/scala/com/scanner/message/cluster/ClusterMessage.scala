package com.scanner.message.cluster

import com.scanner.message.core.{Message, Response}

/**
  * Created by Iurii on 07-06-2017.
  */
trait ClusterMessage extends Message
trait ClusterResponse extends Response

case object GetClusterMembersMessage extends ClusterMessage
case class GetClusterMembersResponse(members: scala.collection.mutable.Set[String]) extends ClusterResponse
