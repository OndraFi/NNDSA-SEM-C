package main.java.gui.components;

import main.java.City;
import main.java.grid.GridFile;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GridIndexSearchForm extends JPanel{

    private final GridFile<City> gridFile;
    private final GraphPanel graphPanel;
    JTextField rectangleSearchX1;
    JTextField rectangleSearchY1;
    JTextField rectangleSearchX2;
    JTextField rectangleSearchY2;

    JTextField pointSearchX;
    JTextField pointSearchY;


    public GridIndexSearchForm(GridFile<City> gridFile, GraphPanel graphPanel) {
        this.gridFile = gridFile;
        this.graphPanel = graphPanel;
        initComponent();
    }

    private void initComponent() {
        setLayout(new GridLayout(0, 3));
        rectangleSearchX1 = new JTextField(20);
        rectangleSearchY1 = new JTextField(20);
        rectangleSearchX2 = new JTextField(20);
        rectangleSearchY2 = new JTextField(20);
        JLabel xLabel = new JLabel("X:");
        JLabel yLabel = new JLabel("Y:");

        add(xLabel);
        add(rectangleSearchX1);
        add(rectangleSearchX2);
        add(yLabel);
        add(rectangleSearchY1);
        add(rectangleSearchY2);

        JButton searchButton = new JButton("Hledat");
        searchButton.addActionListener(e -> {
            try {
                search();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        add(searchButton);

        pointSearchX = new JTextField(20);
        pointSearchY = new JTextField(20);
        JLabel pointSearchLabel = new JLabel("bodové vyhledávání:");
        JButton searchPointBtn = new JButton("Hledat");
        JLabel nothing = new JLabel("");
        JLabel nothing2 = new JLabel("");
        add(nothing);
        add(nothing2);
        searchPointBtn.addActionListener(e -> {
            try {
                searchPoint();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        add(pointSearchLabel);
        add(pointSearchX);
        add(pointSearchY);
        add(searchPointBtn);
    };

    private void search() throws IOException {
        int x1 = Integer.parseInt(this.rectangleSearchX1.getText());
        int y1 = Integer.parseInt(this.rectangleSearchY1.getText());
        int x2 = Integer.parseInt(this.rectangleSearchX2.getText());
        int y2 = Integer.parseInt(this.rectangleSearchY2.getText());

        if(x1 < 0 || x1 >= gridFile.getWidth() || y1 < 0 || y1 >= gridFile.getHeight() || x2 < 0 || x2 >= gridFile.getWidth() || y2 < 0 || y2 >= gridFile.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatné souřadnice");
            return;
        }

        ArrayList<City> cities = gridFile.findElementBySegment(x1, y1, x2, y2);

        graphPanel.setCitiesSearchedInGraphIndex(cities);
        graphPanel.setSearchDimensions(x1, y1, x2, y2);
        graphPanel.repaint();
    }

    private void searchPoint() throws IOException {
        int x = Integer.parseInt(this.pointSearchX.getText());
        int y = Integer.parseInt(this.pointSearchY.getText());

        if(x < 0 || x >= gridFile.getWidth() || y < 0 || y >= gridFile.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatne souradnice");
        }

        City city = gridFile.findElementsByCoordinates(x,y);
        graphPanel.setFoundCityByCoordinates(city);
        graphPanel.repaint();

        if(city == null)
            JOptionPane.showMessageDialog(null,"Mesto nebylo nalezeno");
    }
}
