package com.scanner.cluster

import com.typesafe.config.ConfigFactory

/**
  * Created by Iurii on 07-06-2017.
  */
trait ClusterConfig {
  val clusterConfig = ConfigFactory.load()
  val systemName = clusterConfig.getString("scanner.system.name")
  val serviceName = clusterConfig.getString("scanner.service.name")
}