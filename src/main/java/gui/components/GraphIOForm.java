package main.java.gui.components;

import main.java.City;
import main.java.GraphIO;
import main.java.Road;
import main.java.graph.Graph;
import main.java.grid.GridFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class GraphIOForm extends JPanel {
    private final GridFile<City> gridFile;
    private final Graph<String,City,Road> graph;
    private final GraphIO graphIO;
    private final JButton loadButton;
    private final JButton saveButton;
    private final java.util.List<Runnable> loadListeners = new ArrayList<>();

    public GraphIOForm(GridFile<City> gridFile, Graph<String,City,Road> graph) {
        this.gridFile = gridFile;
        this.graph = graph;
        this.graphIO = new GraphIO(this.gridFile, graph);
        setLayout(new GridLayout(0, 2));
        loadButton = new JButton("Load Graph");
        saveButton = new JButton("Save Graph");

        loadButton.addActionListener(new LoadButtonListener());
        saveButton.addActionListener(new SaveButtonListener());

        add(loadButton);
        add(saveButton);
    }

    public void addLoadListener(Runnable listener) {
        loadListeners.add(listener);
    }

    private void notifyLoadListeners() {
        for (Runnable listener : loadListeners) {
            listener.run();
        }
    }

    private class LoadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                loadFromFile(fileChooser.getSelectedFile().getPath());
                notifyLoadListeners();
                JOptionPane.showMessageDialog(null, "Graph loaded successfully!");
            }
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                saveToFile(fileChooser.getSelectedFile().getPath());
                JOptionPane.showMessageDialog(null, "Graph saved successfully!");
            }
        }
    }

    private void loadFromFile(String path){
        try {
            graphIO.importFromCSV(path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, "Error reading CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToFile(String path){
        try {
            graphIO.saveToCSV(path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}