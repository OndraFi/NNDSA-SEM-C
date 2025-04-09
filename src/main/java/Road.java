package main.java;

public class Road {
    private Boolean accessible;
    private final int weight;
    public Road(int weight) {
        this.accessible = true;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public Boolean isAccessible() {
        return accessible;
    }

    public void blockRoad() {
        this.accessible = false;
    }

    public void unblockRoad() {
        this.accessible = true;
    }
}
