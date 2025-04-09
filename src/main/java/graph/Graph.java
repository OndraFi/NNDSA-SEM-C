package main.java.graph;

import main.java.City;
import main.java.Road;

import java.util.*;

public class Graph<K,TVertex, TEdge> {

    private final Map<K, Vertex> vertices = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    private class Vertex {
        private final K key;
        private final TVertex data;
        private final List<Edge>  adjencyList = new ArrayList<>();

        public Vertex(K key, TVertex data) {
            this.data = data;
            this.key = key;
        }

        public K getKey() {
            return this.key;
        }

        public TVertex getData() {
            return data;
        }

        public void addEdge(Edge edge) {
            this.adjencyList.add(edge);
        }

        public List<Edge> getAdjacentEdges() {
            return this.adjencyList;
        }

    }
    private class Edge {
        K vertex1Key;
        K vertex2Key;
        TEdge data;

        public Edge(K vertex1Key,K vertex2Key, TEdge data) {
            this.vertex1Key = vertex1Key;
            this.vertex2Key = vertex2Key;
            this.data = data;
        }

        public TEdge getData() {
            return data;
        }

        public K getVertex1Key() {
            return vertex1Key;
        }

        public K getVertex2Key() {
            return vertex2Key;
        }
    }

    public Map<K, TVertex> getVertices() {
        Map<K, TVertex> vertexDataMap = new HashMap<>();
        for (Map.Entry<K, Vertex> entry : vertices.entrySet()) {
            vertexDataMap.put(entry.getKey(), entry.getValue().getData());
        }
        return vertexDataMap;
    }

    public List<EdgeData<K, TEdge>> getEdges() {
        List<EdgeData<K, TEdge>> edgeDataList = new ArrayList<>();
        for (Edge edge : edges) {
            edgeDataList.add(new EdgeData<>(edge.getVertex1Key(), edge.getVertex2Key(), edge.getData()));
        }
        return edgeDataList;
    }

    public void addVertex(K key, TVertex data) {
        Vertex vertex = new Vertex(key,data);
        vertices.put(vertex.getKey(), vertex);
    }

    public void addEdge(K vertex1Key, K vertex2Key, TEdge data) throws IllegalArgumentException {
        Edge edge = new Edge(vertex1Key, vertex2Key, data);
        Vertex vertex1 = vertices.get(edge.getVertex1Key());
        Vertex vertex2 = vertices.get(edge.getVertex2Key());

        if (vertex1 == null || vertex2 == null) {
            throw new IllegalArgumentException("Both vertices must exist in the graph");
        }

        vertex1.addEdge(edge);
        vertex2.addEdge(edge);
        edges.add(edge);
    }

    public void removeEdge(K vertex1Key, K vertex2Key) {
        Vertex vertex1 = vertices.get(vertex1Key);
        Vertex vertex2 = vertices.get(vertex2Key);
        if (vertex1 == null || vertex2 == null) {
            throw new IllegalArgumentException("Both vertices must exist in the graph");
        }

        Edge edgeToRemove = null;
        for (Edge edge : vertex1.getAdjacentEdges()) {
            if ((edge.getVertex1Key().equals(vertex1Key) && edge.getVertex2Key().equals(vertex2Key)) ||
                    (edge.getVertex1Key().equals(vertex2Key) && edge.getVertex2Key().equals(vertex1Key))) {
                edgeToRemove = edge;
                break;
            }
        }

        if (edgeToRemove == null) {
            throw new NoSuchElementException("Edge not found between the specified vertices");
        }

        vertex1.getAdjacentEdges().remove(edgeToRemove);
        vertex2.getAdjacentEdges().remove(edgeToRemove);
        edges.remove(edgeToRemove);
    }

    public TVertex getVertex(K key) {
        return vertices.get(key).getData();
    }

    public Map<K,TEdge> getVertexAdjacentEdges(K key) {
        Vertex vertex = vertices.get(key);
        Map<K,TEdge> adjacentEdges = new HashMap<>();
        for (Edge edge : vertex.getAdjacentEdges()) {
            if (edge.getVertex1Key().equals(key)) {
                adjacentEdges.put(edge.getVertex2Key(), edge.getData());
            } else {
                adjacentEdges.put(edge.getVertex1Key(), edge.getData());
            }
        }
        return adjacentEdges;
    }

    public TEdge getEdge(K vertexKey1, K vertexKey2) throws NoSuchElementException, IllegalArgumentException {
        Vertex vertex1 = findVertex(vertexKey1);
        Vertex vertex2 = findVertex(vertexKey2);

        if (vertex1 == null || vertex2 == null) {
            throw new IllegalArgumentException("Both vertices must exist in the graph");
        }

        List<Edge> edges = vertex1.getAdjacentEdges();
        for (Edge edge : edges) {
            if ((edge.getVertex1Key().equals(vertexKey1) && edge.getVertex2Key().equals(vertexKey2)) ||
                    (edge.getVertex1Key().equals(vertexKey2) && edge.getVertex2Key().equals(vertexKey1))) {
                return edge.getData();
            }
        }

        throw new NoSuchElementException("Edge not found between the specified vertices");
    }

    private Vertex findVertex(K key) {
        return vertices.get(key);
    }
}