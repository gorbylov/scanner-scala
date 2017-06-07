package com.scanner.cluster

import akka.actor.{ActorSystem, Props}

/**
  * Created by Iurii on 07-06-2017.
  */
object ClusterSeedApp extends App with ClusterConfig {
  val system = ActorSystem(systemName)
  system.actorOf(Props[ClusterSeed], "clusterSeed")
}
