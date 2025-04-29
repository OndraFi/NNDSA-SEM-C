package main.java.gui.components;

import main.java.App;
import main.java.City;
import main.java.Road;
import main.java.graph.Graph;
import main.java.grid.GridFile;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class CityForm extends JPanel {
    private JTextField cityNameField, xCoordField, yCoordField,populationField;
    private JButton addCityButton;
    private final GridFile<City> gridFile;
    private final Graph<String,City,Road> graph;
    private java.util.List<Runnable> cityListeners = new ArrayList<>();

    public CityForm(GridFile<City> gridFile, Graph<String,City, Road> graph) {
        this.gridFile = gridFile;
        this.graph = graph;
        initializeComponents();
    }

    public void addCityListener(Runnable listener) {
        cityListeners.add(listener);
    }

    private void notifyCityListeners() {
        for (Runnable listener : cityListeners) {
            listener.run();
        }
    }

    private void initializeComponents() {
        setLayout(new GridLayout(0, 2));

        cityNameField = new JTextField(20);
        populationField = new JTextField(20);
        xCoordField = new JTextField(20);
        yCoordField = new JTextField(20);

        add(new JLabel("City Name:"));
        add(cityNameField);
        add(new JLabel("City Population:"));
        add(populationField);
        add(new JLabel("X Coordinate:"));
        add(xCoordField);
        add(new JLabel("Y Coordinate:"));
        add(yCoordField);

        addCityButton = new JButton("Add City");
        addCityButton.addActionListener(e -> addCity());
        add(addCityButton);
    }

    private void addCity() {
        String cityName = cityNameField.getText();
        String populationString = populationField.getText();
        String xCoord = xCoordField.getText();
        String yCoord = yCoordField.getText();

        try {
            int pupulation = Integer.parseInt(populationString);
            int x = Integer.parseInt(xCoord);
            int y = Integer.parseInt(yCoord);

            if (!cityName.isEmpty() && !xCoord.isEmpty() && !yCoord.isEmpty()) {
                City city = new City(cityName,pupulation, x, y);
                gridFile.add(city);
                graph.addVertex(App.generateKey(),city);
                cityNameField.setText("");
                populationField.setText("");
                xCoordField.setText("");
                yCoordField.setText("");
                JOptionPane.showMessageDialog(this, "City added successfully!");
            }
            notifyCityListeners();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid coordinates", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex){
            JOptionPane.showMessageDialog(this, "V miste je bud mesto nebo rez: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}