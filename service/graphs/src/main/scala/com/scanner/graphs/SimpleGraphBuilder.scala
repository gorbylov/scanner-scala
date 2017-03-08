package com.scanner.graphs

/**
  * Created by Iurii on 07-03-2017.
  */
class SimpleGraphBuilder[V] extends GraphBuilder[V] {

  override def build(relations: List[(V, V)])(implicit depth: Int = 1): Graph[V] = {
    val map = relations.groupBy(_._1).mapValues(_.map(_._2))
    val graph: Map[V, List[Conn]] = buildGraph(map, depth)
    SimpleGraph(graph)
  }

  private def buildGraph(map: Map[V, List[V]], depth: Int): Map[V, List[Conn]] = {
    val vertexes = map.keys
    // building graph with specified depth
    val graph = vertexes.flatMap(parentVertex => {
      val firstConnectionsLevel =
        if (depth > 0)
          map.getOrElse(parentVertex, List())
            .map(childVertex => parentVertex :: childVertex :: Nil)
        else if (depth == 0) List(List(parentVertex))
        else List()
      dig(firstConnectionsLevel, map, depth - 1)
    })
      .toList
      .groupBy(_.head)
    // attaching connections with other depth
    if (depth <= 0) {
      graph
    } else {
      val deeperGraph = buildGraph(map, depth - 1)
      (graph.toSeq ++ deeperGraph.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2).toList)
    }
  }

  private def dig(connections: List[Conn], map: Map[V, Conn], depth: Int): List[Conn] = {
    if (depth <= 0) {
      connections
    } else {
      val deeperConnectionsLevel = connections.flatMap(connection =>
        map.getOrElse(connection.last, List())
          .filter(vertex => !connection.contains(vertex))
          .map(vertex => connection :+ vertex)
      )
      dig(deeperConnectionsLevel, map, depth - 1)
    }
  }
}

object SimpleGraphBuilder{
  def apply[V]() = new SimpleGraphBuilder[V]()
}