package com.scanner.service.core.graphs;

import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Iurii on 07-03-2017.
 */
public class Test {

  public static void main(String[] args) {
    Map<String, List<String>> map = new HashMap<>();
    map.put("A", Arrays.asList("B", "C", "D", "E", "F"));
    map.put("B", Arrays.asList("A", "C", "E"));
    map.put("C", Arrays.asList("A"));
    map.put("D", Arrays.asList("A", "B", "C", "E", "F"));
    map.put("E", Arrays.asList("A", "D", "F"));
    map.put("F", Arrays.asList("B", "C"));

    List<Tuple2<String, String>> tupples = map.entrySet().stream().map(entry -> entry
      .getValue().stream()
      .map(str ->
        new Tuple2<>(entry.getKey(), str))
      .collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList());



    GraphBuilder<String> graph = new SimpleGraphBuilder<>();
    graph.build(scala.collection.JavaConverters.asScalaBuffer(tupples).toList(), 1);
  }
}
