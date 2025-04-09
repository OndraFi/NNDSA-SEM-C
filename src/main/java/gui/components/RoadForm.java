package main.java.gui.components;

import main.java.City;
import main.java.Road;
import main.java.graph.EdgeData;
import main.java.graph.Graph;
import main.java.gui.ComboBoxItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoadForm extends JPanel {
    private JComboBox<ComboBoxItem> vertex1ComboBox, vertex2ComboBox, blockedEdgesComboBox, unblockedEdgesComboBox;
    private JTextField weightField;
    private JButton addEdgeButton, removeEdgeButton, blockEdgeButton, unblockEdgeButton;
    private final Graph<String, City, Road> graph;
    private List<Runnable> roadListeners = new ArrayList<>();

    public RoadForm( Graph<String,City,Road> graph) {
        this.graph = graph;
        initializeComponents();
        mapVerticesToComboBoxes();
    }

    public void addRoadListener(Runnable listener) {
        roadListeners.add(listener);
    }

    private void notifyRoadListeners() {
        for (Runnable listener : roadListeners) {
            listener.run();
        }
    }

    private void initializeComponents() {
        setLayout(new GridLayout(0, 2));

        vertex1ComboBox = new JComboBox<>();
        vertex2ComboBox = new JComboBox<>();
        blockedEdgesComboBox = new JComboBox<>();
        unblockedEdgesComboBox = new JComboBox<>();
        weightField = new JTextField(5);
        addEdgeButton = new JButton("Add Edge");
        removeEdgeButton = new JButton("Remove Edge");
        blockEdgeButton = new JButton("Block Edge");
        unblockEdgeButton = new JButton("Unblock Edge");

        add(new JLabel("From:"));
        add(vertex1ComboBox);
        add(new JLabel("To:"));
        add(vertex2ComboBox);
        add(new JLabel("Weight:"));
        add(weightField);
        add(addEdgeButton);
        add(removeEdgeButton);
        add(new JLabel("Blocked Edges:"));
        add(blockedEdgesComboBox);
        add(new JLabel("Unblocked Edges:"));
        add(unblockedEdgesComboBox);
        add(blockEdgeButton);
        add(unblockEdgeButton);

        addEdgeButton.addActionListener(e -> addEdge());
        removeEdgeButton.addActionListener(e -> removeEdge());
        blockEdgeButton.addActionListener(e -> blockEdge());
        unblockEdgeButton.addActionListener(e -> unblockEdge());
    }

    public void mapVerticesToComboBoxes() {
        vertex1ComboBox.removeAllItems();
        vertex2ComboBox.removeAllItems();
        for (Map.Entry<String, City> entry : this.graph.getVertices().entrySet()) {
            String key = entry.getKey();
            City city = entry.getValue();
            ComboBoxItem item = new ComboBoxItem(key, city.getName());
            vertex1ComboBox.addItem(item);
            vertex2ComboBox.addItem(item);
        }
    }

    public void mapEdgesToComboBoxes() {
        blockedEdgesComboBox.removeAllItems();
        unblockedEdgesComboBox.removeAllItems();
        for (EdgeData<String, Road> edgeData : this.graph.getEdges()) {
            Road road = edgeData.getData();
            String vertex1Key = edgeData.getVertex1Key();
            String vertex2Key = edgeData.getVertex2Key();
            City city1 = this.graph.getVertex(vertex1Key);
            City city2 = this.graph.getVertex(vertex2Key);
            ComboBoxItem item = new ComboBoxItem(vertex1Key + ";" + vertex2Key, city1.getName() + " do " + city2.getName());
            if (!road.isAccessible()) {
                blockedEdgesComboBox.addItem(item);
            } else {
                unblockedEdgesComboBox.addItem(item);
            }
        }
    }

    private void blockEdge() {
        ComboBoxItem selectedItem = (ComboBoxItem) unblockedEdgesComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an edge to block.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] vertices = selectedItem.getKey().split(";");
        String vertex1Key = vertices[0];
        String vertex2Key = vertices[1];
        try {
            Road road = graph.getEdge(vertex1Key, vertex2Key);
            road.blockRoad();
            JOptionPane.showMessageDialog(this, "Edge blocked successfully!");
            notifyRoadListeners();
            mapEdgesToComboBoxes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error blocking edge: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unblockEdge() {
        ComboBoxItem selectedItem = (ComboBoxItem) blockedEdgesComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an edge to block.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] vertices = selectedItem.getKey().split(";");
        String vertex1Key = vertices[0];
        String vertex2Key = vertices[1];
        try {
            Road road = graph.getEdge(vertex1Key, vertex2Key);
            road.unblockRoad();
            JOptionPane.showMessageDialog(this, "Edge blocked successfully!");
            mapEdgesToComboBoxes();
            notifyRoadListeners();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error blocking edge: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEdge() {
        ComboBoxItem item1 = (ComboBoxItem) vertex1ComboBox.getSelectedItem();
        ComboBoxItem item2 = (ComboBoxItem) vertex2ComboBox.getSelectedItem();
        if (item1 == null || item2 == null) {
            JOptionPane.showMessageDialog(this, "Please select two vertices.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String vertex1Key = item1.getKey();
        String vertex2Key = item2.getKey();
        String weightStr = weightField.getText();
        try {
            int weight = Integer.parseInt(weightStr);
            if (!vertex1Key.equals(vertex2Key)) {
                this.graph.addEdge(vertex1Key, vertex2Key, new Road(weight));
                JOptionPane.showMessageDialog(this, "Edge added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Cannot add edge between the same vertex.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            mapEdgesToComboBoxes();
            notifyRoadListeners();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid weight.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeEdge(){
        ComboBoxItem item1 = (ComboBoxItem) vertex1ComboBox.getSelectedItem();
        ComboBoxItem item2 = (ComboBoxItem) vertex2ComboBox.getSelectedItem();
        if (item1 == null || item2 == null) {
            JOptionPane.showMessageDialog(this, "Please select two vertices.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String vertex1Key = item1.getKey();
        String vertex2Key = item2.getKey();
        try {
            graph.removeEdge(vertex1Key, vertex2Key);
            JOptionPane.showMessageDialog(this, "Edge removed successfully!");
            mapEdgesToComboBoxes();
            notifyRoadListeners();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error removing edge: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}