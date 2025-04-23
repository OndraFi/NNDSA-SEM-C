package main.java.gui.components;

import main.java.City;
import main.java.grid.GridIndex;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class SaveGridIndexToFileForm extends JPanel {
    private final JButton loadButton;
    private final JButton saveButton;
    private JTextField fileNameField;
    private GridIndex<City> gridIndex;
    private java.util.List<Runnable> loadListeners = new ArrayList<>();


    public SaveGridIndexToFileForm(GridIndex<City> gridIndex) {
        this.gridIndex = gridIndex;
        setLayout(new GridLayout(0, 2));
        fileNameField = new JTextField(20);
        loadButton = new JButton("Load Grid Index");
        saveButton = new JButton("Save Grid Index");

        loadButton.addActionListener(e -> loadFromFile());
        saveButton.addActionListener(e -> saveToFile());

        add(new JLabel("File Name:"));
        add(fileNameField);
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

    private void loadFromFile() {
        try {

            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                this.gridIndex.loadFromFile(fileChooser.getSelectedFile().getName());
                notifyLoadListeners();
                JOptionPane.showMessageDialog(this, "Grid index loaded successfully!");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToFile() {
        try {
            System.out.println("Saving Grid Index to file..." + this.fileNameField.getText() + ".ser");
            this.gridIndex.saveToFile(this.fileNameField.getText()+ ".ser");
            JOptionPane.showMessageDialog(this, "Grid index saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving grid index: " + e.getMessage());
        }
    }


}
