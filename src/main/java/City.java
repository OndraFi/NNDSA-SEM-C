package main.java;

import main.java.grid.LocationInterface;

public class City implements LocationInterface {

    private final String name;
    private final Location location;

    public City(String name, int x, int y) {
        this.location = new Location(x, y);
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return name;
    }
}
