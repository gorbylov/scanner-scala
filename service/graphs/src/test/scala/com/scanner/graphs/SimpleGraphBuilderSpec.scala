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

    "build graph" in {
      implicit val depth = 0
      val graph = SimpleGraphBuilder().build(relations)
      graph.getConnections("A") shouldBe Some(List(List("A")))
      graph.getConnections("B") shouldBe Some(List(List("B")))
      graph.getConnections("F") shouldBe None
    }
  }

}
