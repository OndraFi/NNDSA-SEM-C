package main.java.gui;

import main.java.City;
import main.java.Road;
import main.java.graph.Graph;
import main.java.grid.GridFile;
import main.java.gui.components.*;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 800;
    private final GridFile<City> gridFile;
    private final Graph<String, City, Road> graph;
    private GraphPanel graphPanel;
    private JPanel sidebar;

    private CityForm cityForm;
    private RoadForm roadForm;
    private GraphIOForm graphIOForm;
    private DijkstraForm dijkstraForm;
    private GridIndexSearchForm gridIndexSearchForm;

    public Gui(GridFile<City> gridFile, Graph<String,City,Road> graph) {
        super("Semestrálí práce C Ondřej Fiala");
        this.gridFile = gridFile;
        this.graph = graph;
        initWindow();
        initializeComponents();
        setVisible(true);
    }

    public GridFile<City> promptGridIndexFileAction() {
        String[] options = {"Načíst existující", "Vytvořit nový"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Chceš načíst existující gridIndex nebo vytvořit nový?",
                "Výběr akce",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // Načíst existující
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                    this.gridFile.loadFromFile(fileName);
                    JOptionPane.showMessageDialog(null, "GridIndex úspěšně načten.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Chyba při načítání: " + e.getMessage());
                    System.exit(1); // nebo nějaké fallback chování
                }
            } else {
                System.exit(0); // zavřít appku když uživatel klikne na cancel
            }
        } else if (choice == 1) {
            // Vytvořit nový
            String fileName = JOptionPane.showInputDialog("Zadej název souboru pro budoucí uložení:");
            if (fileName == null || fileName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Musíš zadat název souboru.");
                System.exit(0);
            }
            this.gridFile.setGridIndexSaveFileName(fileName);
        }

        return null; // fallback
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


        graphPanel = new GraphPanel(gridFile,new Dimension((int) Math.round(PANEL_WIDTH * 0.8), (int) Math.round(PANEL_HEIGHT * 0.8)));
        add(graphPanel, BorderLayout.CENTER);

        addCityForm();
//        addRoadForm();
        addGridIndexSearchForm();
        addGridIndexSaveForm();
//        addGraphIOForm();
//        addDijkstraForm();

    }

    private void addGridIndexSaveForm(){
        SaveGridIndexToFileForm gridIndexSaveForm = new SaveGridIndexToFileForm(gridFile);
        sidebar.add(gridIndexSaveForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addGridIndexSearchForm(){
        gridIndexSearchForm = new GridIndexSearchForm(gridFile,graphPanel);
        sidebar.add(gridIndexSearchForm);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addCityForm() {
        cityForm = new CityForm(this.gridFile,this.graph);
        cityForm.addCityListener(() -> {
//            roadForm.mapVerticesToComboBoxes();
//            dijkstraForm.mapVerticesToComboBoxes();
//            dijkstraForm.changesWareMadeInGraph();
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
        graphIOForm = new GraphIOForm(this.gridFile,graph);
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