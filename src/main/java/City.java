package main.java;

import main.java.grid.LocationInterface;

public class City implements LocationInterface {

    private  String name;
    private  Location location;
    private  int population;

    public City(String name, int population, int x, int y) {
        this.location = new Location(x, y);
        this.name = name;
        this.population = population;
    }

    public City(){

    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return this.name;
    }

    public int getPopulation() {
        return this.population;
    }

    @Override
    public String toString() {
        return name;
    }
}
