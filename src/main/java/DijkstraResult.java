package main.java;

import java.util.Map;

public class DijkstraResult {
    Map<String,String> previous;
    Map<String, Map<String, String>> successors;

    public DijkstraResult(Map<String, String> previous, Map<String, Map<String, String>> successors) {
        this.previous = previous;
        this.successors = successors;
    }

    public Map<String, String> getPrevious() {
        return previous;
    }

    public Map<String, Map<String, String>> getSuccessors() {
        return successors;
    }
}
