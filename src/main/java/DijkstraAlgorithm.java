package main.java;

import main.java.graph.Graph;
import java.util.*;

public class DijkstraAlgorithm {

    public static DijkstraResult dijkstra(Graph<String, City, Road> graph, String startVertexKey) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, Map<String, String>> successors = new HashMap<>();

        // Priority queue to determine the vertex with the shortest distance
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(vertexKey -> distances.getOrDefault(vertexKey, Integer.MAX_VALUE)));

        // Initialize distances and previous
        for (String vertexKey : graph.getVertices().keySet()) {
            distances.put(vertexKey, Integer.MAX_VALUE);
            previous.put(vertexKey, null);
            successors.put(vertexKey, new HashMap<>());
        }

        // Set the distance from the start vertex to itself to 0
        distances.put(startVertexKey, 0);
        queue.add(startVertexKey);

        while (!queue.isEmpty()) {
            String uKey = queue.poll();


            // Explore each adjacent vertex of u
            for (Map.Entry<String,Road> entry : graph.getVertexAdjacentEdges(uKey).entrySet()) {
                String vKey = entry.getKey();
                Road road = entry.getValue();

                if(!road.isAccessible())
                    continue;

                int alt = distances.get(uKey) + road.getWeight();

                if (alt < distances.get(vKey)) {
                    distances.put(vKey, alt);
                    previous.put(vKey, uKey);
                    if (!queue.contains(vKey)) {
                        queue.add(vKey);
                    }

                    // Update successors for each vertex
                    successors.get(uKey).put(vKey, vKey);
                    // Recursively update the successors path
                    updateSuccessors(successors, previous, uKey, vKey);
                }
            }
        }
        printSuccessorMatrix(successors, graph);
        printSecondTable(previous,graph);
        return new DijkstraResult(previous, successors);
    }

    private static void updateSuccessors(Map<String, Map<String, String>> successors, Map<String, String> previous, String current, String next) {
        // Update the successors for the current path
        for (String key : successors.keySet()) {
            if (successors.get(key).containsKey(current)) {
                successors.get(key).put(next, successors.get(key).get(current));
            }
        }
    }

    public static void printSecondTable(Map<String, String> previous, Graph<String, City, Road> graph) {
        Set<String> vertexKeys = graph.getVertices().keySet();
        for(String key: vertexKeys) {
            System.out.print(key);
        }
        System.out.println();
        for (String key : vertexKeys) {
            System.out.print(previous.get(key));
        }
    }

    public static void printSuccessorMatrix(Map<String, Map<String, String>> successors, Graph<String, City, Road> graph) {
        List<String> keys = new ArrayList<>(graph.getVertices().keySet());
        Collections.sort(keys);

        // Tisk hlavičky
        System.out.print("    ");
        for (String key : keys) {
            City city = graph.getVertex(key);
            System.out.print(String.format("%2s ", city.getName()));
        }
        System.out.println();

        // Tisk těla matice
        for (String key : keys) {
            City rowCity = graph.getVertex(key);
            System.out.print(String.format("%2s ", rowCity.getName()));
            Map<String, String> row = successors.get(key);
            for (String colKey : keys) {
                String successorKey = row.get(colKey);
                if (successorKey == null) {
                    System.out.print(" . ");
                } else {
                    City colCity = graph.getVertex(successorKey);
                    System.out.print(String.format("%2s ", colCity.getName()));
                }
            }
            System.out.println();
        }
    }

}