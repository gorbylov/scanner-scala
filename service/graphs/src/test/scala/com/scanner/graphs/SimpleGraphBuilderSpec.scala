package com.scanner.graphs

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Iurii on 07-03-2017.
  */
class SimpleGraphBuilderSpec extends WordSpec with Matchers{

  "SimpleGraphBuilder" should {

    val relations = List(
      "A" -> "B",
      "A" -> "C",
      "A" -> "D",
      "A" -> "E",
      "B" -> "A",
      "B" -> "C",
      "B" -> "E",
      "C" -> "A",
      "D" -> "A",
      "D" -> "B",
      "D" -> "C"
    )

    "build correct graph with 0 depth" in {
      implicit val depth = 0
      val graph = SimpleGraphBuilder().build(relations)
      assertGraphWithDepth(graph, depth)
    }

    "build correct graph with 1 depth" in {
      implicit val depth = 1
      val graph = SimpleGraphBuilder().build(relations)
      assertGraphWithDepth(graph, depth)
    }

    "build correct graph with 2 depth" in {
      implicit val depth = 2
      val graph = SimpleGraphBuilder().build(relations)
      assertGraphWithDepth(graph, depth)
    }

    "build correct graph with 3 depth" in {
      implicit val depth = 3
      val graph = SimpleGraphBuilder().build(relations)
      assertGraphWithDepth(graph, depth)
    }

    "build epty graph with -1 depth" in {
      implicit val depth = -1
      val graph = SimpleGraphBuilder().build(relations)
      assertEmptyGraph(graph)
    }

    "build empty graph with empty list of relations" in {
      implicit val depth = 2
      val graph = SimpleGraphBuilder().build(List[(String, String)]())
      assertEmptyGraph(graph)
    }
  }

  private def assertGraphWithDepth(graph: Graph[String], depth: Int) = {
    for (value <- 0 to depth) {
      value match {
        case 0 =>
          graph.getConnections("A").get.contains(List("A")) shouldBe true
          graph.getConnections("B").get.contains(List("B")) shouldBe true
          graph.getConnections("C").get.contains(List("C")) shouldBe true
          graph.getConnections("D").get.contains(List("D")) shouldBe true
          graph.getConnections("E") shouldBe None
        case 1 =>
          List(List("A", "C"), List("A", "E"), List("A", "B"), List("A", "D")).foreach(list =>
            graph.getConnections("A").get.contains(list) shouldBe true
          )
          List(List("B", "C"), List("B", "A"), List("B", "E")).foreach(list =>
            graph.getConnections("B").get.contains(list) shouldBe true
          )
          List(List("C", "A")).foreach(list =>
            graph.getConnections("C").get.contains(list) shouldBe true
          )
          List(List("D", "B"), List("D", "A"), List("D", "C")).foreach(list =>
            graph.getConnections("D").get.contains(list) shouldBe true
          )
        case 2 =>
          List(List("A", "D", "B"), List("A", "B", "E"), List("A", "B", "C"), List("A", "D", "C")).foreach(list =>
            graph.getConnections("A").get.contains(list) shouldBe true
          )
          List(List("B", "A", "C"), List("B", "C", "A"), List("B", "A", "E"), List("B", "A", "D")).foreach(list =>
            graph.getConnections("B").get.contains(list) shouldBe true
          )
          List(List("C", "A", "D"), List("C", "A", "E"), List("C", "A", "B")).foreach(list =>
            graph.getConnections("C").get.contains(list) shouldBe true
          )
          List(List("D", "A", "B"), List("D", "B", "A"), List("D", "C", "A"), List("D", "A", "E"), List("D", "B", "C"), List("D", "B", "E"), List("D", "A", "C")).foreach(list =>
            graph.getConnections("D").get.contains(list) shouldBe true
          )
        case 3 =>
          List(List("A", "D", "B", "E"), List("A", "D", "B", "C")).foreach(list =>
            graph.getConnections("A").get.contains(list) shouldBe true
          )
          List(List("B", "C", "A", "D"), List("B", "C", "A", "E"), List("B", "A", "D", "C")).foreach(list =>
            graph.getConnections("B").get.contains(list) shouldBe true
          )
          List(List("C", "A", "B", "E"), List("C", "A", "D", "B")).foreach(list =>
            graph.getConnections("C").get.contains(list) shouldBe true
          )
          List(List("D", "A", "B", "C"), List("D", "A", "B", "E"), List("D", "B", "C", "A"), List("D", "B", "A", "C"), List("D", "C", "A", "E"), List("D", "C", "A", "B"), List("D", "B", "A", "E")).foreach(list =>
            graph.getConnections("D").get.contains(list) shouldBe true
          )
      }
    }
  }

  private def assertEmptyGraph(graph: Graph[String]) = {
    graph.getConnections("A") shouldBe None
    graph.getConnections("B") shouldBe None
    graph.getConnections("C") shouldBe None
    graph.getConnections("D") shouldBe None
    graph.getConnections("E") shouldBe None
  }

}
