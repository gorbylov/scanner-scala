package com.scanner.graphs

/**
  * Created by Iurii on 07-03-2017.
  */
class SimpleGraphBuilder[V] extends GraphBuilder[V] {

  override def build(relations: List[(V, V)])(implicit depth: Int = 1): Graph[V] = {
    val map = relations.groupBy(_._1).mapValues(_.map(_._2))
    val vertexes = map.keys

    val graph = vertexes.flatMap(parentVertex => {
      val firstConnectionsLevel =
        if (depth > 0 )
          map.getOrElse(parentVertex, List())
            .map(childVertex => parentVertex :: childVertex :: Nil)
        else List(List(parentVertex))
      dig(firstConnectionsLevel, map, depth - 1)
    })
      .toList
      .groupBy(_.head)

    SimpleGraph(graph)
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