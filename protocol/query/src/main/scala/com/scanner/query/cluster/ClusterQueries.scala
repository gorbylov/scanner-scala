package com.scanner.query.cluster

import com.scanner.query.core.{Query, Response}

/**
  * Created by Iurii on 07-06-2017.
  */
trait ClusterQuery extends Query
trait ClusterResponse extends Response

case object GetClusterMembersQuery extends ClusterQuery
case class GetClusterMembersResponse(
  members: scala.collection.mutable.Set[String]
) extends ClusterResponse
