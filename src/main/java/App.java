package main.java;

import main.java.graph.Graph;
import main.java.grid.GridIndex;
import main.java.gui.Gui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class App {
    Graph<String, City, Road> graph;
    GridIndex<City> gridIndex;

    public App() throws FileNotFoundException {
        this.gridIndex = new GridIndex<>(50, 70, 3,City.class);
        this.graph = new Graph<>();
    }

    public static String generateKey() {
        return UUID.randomUUID().toString();
    }

    public void openWindow() {
        Gui gui = new Gui(gridIndex,graph);
    }

    private void generateAssigmentGraph(){
        City z = new City("Z", 50000, 0, 0);
        City k = new City("K", 42000, 30, 30);
        City s = new City("S", 5000, 100, 40);
        City i = new City("I", 8000, 120, 100);
        City a = new City("A", 3000, 60, 120);
        City x = new City("X", 2000, 80, 140);
        City m = new City("M", 10000, 160, 160);
        City g = new City("G", 7000, 140, 180);
        City u = new City("U", 1500, 20, 200);
        City t = new City("T", 9000, 130, 300);
        City n = new City("N", 6000, 80, 290);
        City f = new City("F", 5500, 100, 250);
        City r = new City("R", 4000, 70, 230);
        City p = new City("P", 2500, 60, 210);
        City w = new City("W", 1800, 40, 270);


        graph.addVertex("Z", z);
        graph.addVertex("K", k);
        graph.addVertex("S", s);
        graph.addVertex("I", i);
        graph.addVertex("A", a);
        graph.addVertex("X", x);
        graph.addVertex("M", m);
        graph.addVertex("G", g);
        graph.addVertex("U", u);
        graph.addVertex("T", t);
        graph.addVertex("N", n);
        graph.addVertex("F", f);
        graph.addVertex("R", r);
        graph.addVertex("P", p);
        graph.addVertex("W", w);


        Road zk = new Road(10);
        graph.addEdge("Z", "K", zk);
        Road ks = new Road(70);
        graph.addEdge("K", "S", ks);
        Road ka = new Road(70);
        graph.addEdge("K", "A", ka);
        Road sa = new Road(60);
        graph.addEdge("S", "A", sa);
        Road si = new Road(40);
        graph.addEdge("S", "I", si);
        Road ix = new Road(60);
        graph.addEdge("I", "X", ix);
        Road ax = new Road(50);
        graph.addEdge("A", "X", ax);
        Road xm = new Road(60);
        graph.addEdge("X", "M", xm);
        Road xg = new Road(65);
        graph.addEdge("X", "G", xg);
        Road xu = new Road(80);
        graph.addEdge("X", "U", xu);
        Road mg = new Road(20);
        graph.addEdge("M", "G", mg);
        Road ug = new Road(90);
        graph.addEdge("U", "G", ug);
        Road gt = new Road(100);
        graph.addEdge("G", "T", gt);
        Road tn = new Road(40);
        graph.addEdge("T", "N", tn);
        Road nf = new Road(50);
        graph.addEdge("N", "F", nf);
        Road nr = new Road(60);
        graph.addEdge("N", "R", nr);
        Road np = new Road(40);
        graph.addEdge("N", "P", np);
        Road pw = new Road(20);
        graph.addEdge("P", "W", pw);
    }

//    public void generateGraph(Graph<String, City, Road> graph) {
//        int gridSize = 8; // 10x10 grid
//        int maxWeight = 100;
//
//        // Create cities in a grid
//        for (int i = 0; i < gridSize; i++) {
//            for (int j = 0; j < gridSize; j++) {
//                String key = "" + (i * gridSize + j + 1);
//                City city = new City(key, i * 100, j * 100); // Assuming 100 units distance between cities
//                graph.addVertex(key, city);
//            }
//        }
//
//        // Create roads with random weights
//        Random rand = new Random();
//        for (int i = 0; i < gridSize; i++) {
//            for (int j = 0; j < gridSize; j++) {
//                String currentCity = "" + (i * gridSize + j + 1);
//                if (i < gridSize - 1) { // Vertical road
//                    String downCity = "" + ((i + 1) * gridSize + j + 1);
//                    int weight = rand.nextInt(maxWeight) + 1;
//                    graph.addEdge(currentCity, downCity, new Road(weight));
//                }
//                if (j < gridSize - 1) { // Horizontal road
//                    String rightCity = "" + (i * gridSize + j + 2);
//                    int weight = rand.nextInt(maxWeight) + 1;
//                    graph.addEdge(currentCity, rightCity, new Road(weight));
//                }
//            }
//        }
//    }

}
