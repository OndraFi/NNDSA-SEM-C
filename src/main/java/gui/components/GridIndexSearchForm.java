package main.java.gui.components;

import main.java.City;
import main.java.grid.GridIndex;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GridIndexSearchForm extends JPanel{

    private final GridIndex<City> gridIndex;
    private final GraphPanel graphPanel;
    JTextField rectangleSearchX1;
    JTextField rectangleSearchY1;
    JTextField rectangleSearchX2;
    JTextField rectangleSearchY2;

    JTextField pointSearchX;
    JTextField pointSearchY;


    public GridIndexSearchForm(GridIndex<City> gridIndex, GraphPanel graphPanel) {
        this.gridIndex = gridIndex;
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
        searchButton.addActionListener(e -> {search();});
        add(searchButton);

        pointSearchX = new JTextField(20);
        pointSearchY = new JTextField(20);
        JLabel pointSearchLabel = new JLabel("bodové vyhledávání:");
        JButton searchPointBtn = new JButton("Hledat");
        JLabel nothing = new JLabel("");
        JLabel nothing2 = new JLabel("");
        add(nothing);
        add(nothing2);
        searchPointBtn.addActionListener(e -> {searchPoint();});
        add(pointSearchLabel);
        add(pointSearchX);
        add(pointSearchY);
        add(searchPointBtn);
    };

    private void search() {
        int x1 = Integer.parseInt(this.rectangleSearchX1.getText());
        int y1 = Integer.parseInt(this.rectangleSearchY1.getText());
        int x2 = Integer.parseInt(this.rectangleSearchX2.getText());
        int y2 = Integer.parseInt(this.rectangleSearchY2.getText());

        if(x1 < 0 || x1 >= gridIndex.getWidth() || y1 < 0 || y1 >= gridIndex.getHeight() || x2 < 0 || x2 >= gridIndex.getWidth() || y2 < 0 || y2 >= gridIndex.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatné souřadnice");
            return;
        }

        ArrayList<City> cities = gridIndex.findElementBySegment(x1, y1, x2, y2);

        graphPanel.setCitiesSearchedInGraphIndex(cities);
        graphPanel.setSearchDimensions(x1, y1, x2, y2);
        graphPanel.repaint();
    }

    private void searchPoint() {
        int x = Integer.parseInt(this.pointSearchX.getText());
        int y = Integer.parseInt(this.pointSearchY.getText());

        if(x < 0 || x >= gridIndex.getWidth() || y < 0 || y >= gridIndex.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatne souradnice");
        }

        City city = gridIndex.findElementsByCoordinates(x,y);
        graphPanel.setFoundCityByCoordinates(city);
        graphPanel.repaint();

        if(city == null)
            JOptionPane.showMessageDialog(null,"Mesto nebylo nalezeno");
    }
}
