package com.scanner.service.core.graphs

/**
  * Created by Iurii on 07-03-2017.
  */
trait GraphBuilder[V] {

  def build(connections: List[(V, V)])(implicit depth: Int = 1): Graph[V]
}

case class Graph[V](graph: Map[V, List[List[(V)]]])