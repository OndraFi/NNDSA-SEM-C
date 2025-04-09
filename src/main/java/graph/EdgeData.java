package main.java.graph;

public class EdgeData<K, TEdge> {
    private final K vertex1Key;
    private final K vertex2Key;
    private final TEdge data;

    public EdgeData(K vertex1Key, K vertex2Key, TEdge data) {
        this.vertex1Key = vertex1Key;
        this.vertex2Key = vertex2Key;
        this.data = data;
    }

    public K getVertex1Key() {
        return vertex1Key;
    }

    public K getVertex2Key() {
        return vertex2Key;
    }

    public TEdge getData() {
        return data;
    }
}