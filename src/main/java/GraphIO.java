package main.java;

import main.java.graph.EdgeData;
import main.java.graph.Graph;
import main.java.grid.GridIndex;

import java.io.*;

public class GraphIO {

    private final GridIndex<City> gridIndex;
    private final Graph<String, City, Road> graph;

    public GraphIO(GridIndex<City> gridIndex, Graph<String,City,Road> graph) {
        this.gridIndex = gridIndex;
        this.graph = graph;
    }

    public void importFromCSV(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts[0].equals("v")) {
                    String key = parts[1];
                    String cityName = parts[2];
                    int x = Integer.parseInt(parts[3]);
                    int y = Integer.parseInt(parts[4]);
                    City city = new City(cityName, 50000,x, y);
                    gridIndex.add(city);
                    graph.addVertex(key, city);
                } else if (parts[0].equals("e")) {
                    String from = parts[1];
                    String to = parts[2];
                    int weight = Integer.parseInt(parts[3]);
                    graph.addEdge(from, to, new Road(weight));
                }
            }
    }

    public void saveToCSV(String fileName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

        try {
            for (String key : graph.getVertices().keySet()) {
                City city = graph.getVertex(key);
                Location location = city.getLocation();
                bw.write("v;" + key + ";" + city.getName() + ";" + location.getX() + ";" + location.getY());
                bw.newLine();
            }
            for (EdgeData<String, Road> edgeData : graph.getEdges()) {
                bw.write("e;" + edgeData.getVertex1Key() + ";" + edgeData.getVertex2Key() + ";" + edgeData.getData().getWeight());
                bw.newLine();
            }
        } finally {
            bw.close();
        }
    }

}
