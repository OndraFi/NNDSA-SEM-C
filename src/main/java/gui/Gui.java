package main.java.gui;

import main.java.City;
import main.java.Road;
import main.java.graph.Graph;
import main.java.grid.GridIndex;
import main.java.gui.components.*;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 800;
    private final GridIndex<City> gridIndex;
    private final Graph<String, City, Road> graph;
    private GraphPanel graphPanel;
    private JPanel sidebar;

    private CityForm cityForm;
    private RoadForm roadForm;
    private GraphIOForm graphIOForm;
    private DijkstraForm dijkstraForm;
    private GridIndexSearchForm gridIndexSearchForm;

    public Gui( GridIndex<City> gridIndex, Graph<String,City,Road> graph) {
        super("Semestrálí práce C Ondřej Fiala");
        this.gridIndex = gridIndex;
        this.graph = graph;
        initWindow();
        initializeComponents();
        setVisible(true);
    }

    private void initWindow() {
        setSize(PANEL_WIDTH, PANEL_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        add(sidebar, BorderLayout.EAST);


        graphPanel = new GraphPanel(gridIndex, graph,new Dimension((int) Math.round(PANEL_WIDTH * 0.8), (int) Math.round(PANEL_HEIGHT * 0.8)));
        add(graphPanel, BorderLayout.CENTER);

        addCityForm();
//        addRoadForm();
        addGridIndexSearchForm();
//        addGraphIOForm();
//        addDijkstraForm();

    }

    private void addGridIndexSearchForm(){
        gridIndexSearchForm = new GridIndexSearchForm(gridIndex,graphPanel);
        sidebar.add(gridIndexSearchForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addCityForm() {
        cityForm = new CityForm(this.gridIndex,this.graph);
        cityForm.addCityListener(() -> {
            roadForm.mapVerticesToComboBoxes();
            dijkstraForm.mapVerticesToComboBoxes();
            dijkstraForm.changesWareMadeInGraph();
            graphPanel.setCitiesSearchedInGraphIndex(null);
            graphPanel.setSearchDimensions(0,0,0,0);
            repaint();
        });
        sidebar.add(cityForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addRoadForm() {
        roadForm = new RoadForm(this.graph);
        roadForm.addRoadListener(() -> {
            dijkstraForm.changesWareMadeInGraph();
            repaint();
        });
        sidebar.add(roadForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addGraphIOForm() {
        graphIOForm = new GraphIOForm(this.gridIndex,graph);
        graphIOForm.addLoadListener(() -> {
            roadForm.mapVerticesToComboBoxes();
            roadForm.mapEdgesToComboBoxes();
            dijkstraForm.mapVerticesToComboBoxes();
            dijkstraForm.changesWareMadeInGraph();
            repaint();
        });
        sidebar.add(graphIOForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addDijkstraForm() {
        dijkstraForm = new DijkstraForm(this.graph, graphPanel);
        sidebar.add(dijkstraForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

}