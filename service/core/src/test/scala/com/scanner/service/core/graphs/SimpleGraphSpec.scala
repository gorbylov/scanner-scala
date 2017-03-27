package com.scanner.service.core.graphs

import com.scanner.service.core.graphs.SimpleGraphBuilder
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by Iurii on 08-03-2017.
  */
class SimpleGraphSpec extends WordSpec with Matchers {

  "SimpleGraph" should {
    implicit val depth = 3
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
    val graph = SimpleGraphBuilder().build(relations)

    "find all connections between A and A with depth 3" in {
      val connections = graph.search("A", "A", (a: (String, String)) => -a.hashCode())
      val expected = List(List("A"))
      connections == expected shouldBe true
    }

    "find all connections between A and B with depth 3" in {
      val connections = graph.search("A", "B", (a: (String, String)) => -a.hashCode())
      val expected = List(List("A", "B"), List("A", "D", "B"))
      connections == expected shouldBe true
    }

    "find all connections between A and C with depth 3" in {
      val connections = graph.search("A", "C", (a: (String, String)) => -a.hashCode())
      val expected = List(List("A", "C"), List("A", "B", "C"), List("A", "D", "C"), List("A", "D", "B", "C"))
      connections == expected shouldBe true
    }

    "get empty connections between A and F with depth 3" in {
      val connections = graph.search("A", "F", (a: (String, String)) => -a.hashCode())
      val expected = List()
      connections == expected shouldBe true
    }

    "get empty connections between F and A with depth 3" in {
      val connections = graph.search("F", "A", (a: (String, String)) => -a.hashCode())
      val expected = List()
      connections == expected shouldBe true
    }
  }
}
