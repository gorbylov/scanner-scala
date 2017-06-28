package com.scanner.service.core.graphs

/**
  * Created by IGorbylov on 08.03.2017.
  */
class SimpleGraph[V](graph: Map[V, List[List[V]]]) extends Graph[V] {

  override def search(from: V, to: V)(edgeWeightResolver: ((V, V)) => BigDecimal): List[Conn] = {
    graph.get(from).fold(List[Conn]()){ connections =>
      connections
        .filter(connection => connection.contains(to))
        .sorted(Ordering.fromLessThan[Conn](_.indexOf(to) < _.indexOf(to)))
        .sorted(Ordering.fromLessThan[Conn]((conn1, conn2) => {
          val conn1Weight = conn1.zip(conn1.tail)
            .map(edgeWeightResolver)
            .sum(Numeric.BigDecimalIsFractional)
          val conn2Weight = conn2.zip(conn2.tail).map(edgeWeightResolver)
            .sum(Numeric.BigDecimalIsFractional)
          conn1Weight < conn2Weight
        }))
        .map(connection => connection.slice(0, connection.indexOf(to) + 1))
        .distinct
    }
  }

  override def getConnections(vertex: V): Option[List[Conn]] = graph.get(vertex)

  override def isEmpty(): Boolean = graph.isEmpty

  override def toString: String = graph.toString
}

object SimpleGraph{
  def apply[V](graph: Map[V, List[List[V]]]) = new SimpleGraph(graph)
}