package com.scanner.service.core.graphs

/**
  * Created by Iurii on 07-03-2017.
  */
trait Graphs[V] {
  type Conn = List[V]
}

trait GraphBuilder[V] extends Graphs[V] {
  def build(relations: List[(V, V)])(implicit depth: Int): Graph[V]
}

trait Graph[V] extends Graphs[V] {
  def search(from: V, to: V, edgeWeightResolver: ((V, V)) => BigDecimal) : List[Conn]
  def getConnections(vertex: V): Option[List[Conn]]
  def isEmpty(): Boolean
}

object Graph {
  def build[V](relations: List[(V, V)]): Graph[V] = SimpleGraphBuilder().build(relations)
  def empty[V]: Graph[V] = SimpleGraph(Map.empty)
}