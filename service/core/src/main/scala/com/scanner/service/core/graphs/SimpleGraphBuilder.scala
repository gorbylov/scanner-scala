package com.scanner.service.core.graphs

/**
  * Created by Iurii on 07-03-2017.
  */
class SimpleGraphBuilder[V] extends GraphBuilder[V] {


  // (a -> b, a -> c, b -> a, c -> d)
  // (a -> (b, c), b -> (a), c -> (d))
  override def build(connections: List[(V, V)])(implicit depth: Int): Graph[V] = {
    val map = connections.groupBy(_._1).mapValues(_.map(_._2))

    Graph(Map())
  }
}