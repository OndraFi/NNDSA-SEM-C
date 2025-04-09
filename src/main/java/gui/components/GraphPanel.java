package main.java.gui.components;

import main.java.City;
import main.java.Road;
import main.java.graph.EdgeData;
import main.java.graph.Graph;
import main.java.Location;
import main.java.grid.GridIndex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphPanel extends JPanel {
    private final Graph<String, City, Road> graph;
    private final GridIndex<City> gridIndex;
    private double scaleX, scaleY;
    private final double zoomFactor = 1.0;
    private Point dragStartScreen;
    private Point dragEndScreen;
    private final AffineTransform coordTransform = new AffineTransform();
    private ArrayList<String> path;

    private ArrayList<City> citiesSearchedInGraphIndex; // najité města v gridIndexu
    private int searchRectangleX1, searchRectangleY1, searchRectangleX2, searchRectangleY2; // dimenze vyhledávaného obdélníku
    private City foundCityByCoordinates; // najité město pomocí bodu

    public GraphPanel( GridIndex<City> gridIndex, Graph<String,City,Road> graph, Dimension size) {
        this.graph = graph;
        this.gridIndex = gridIndex;
        this.setPreferredSize(size);
        calculateScaleFactors();
        setupMouseWheelZoom();
        setupMousePan();
    }


    public void setFoundCityByCoordinates(City foundCityByCoordinates) {
        this.foundCityByCoordinates = foundCityByCoordinates;
    }

    public void setSearchDimensions(int x1, int y1, int x2, int y2) {
        this.searchRectangleX1 = x1;
        this.searchRectangleY1 = y1;
        this.searchRectangleX2 = x2;
        this.searchRectangleY2 = y2;
    }

    public void setCitiesSearchedInGraphIndex(ArrayList<City> citiesSearchedInGraphIndex) {
        this.citiesSearchedInGraphIndex = citiesSearchedInGraphIndex;
    }

    public void setPath(ArrayList<String> path) {
        this.path = path;
        repaint();
    }

    private void calculateScaleFactors() {
        double maxX = 0;
        double maxY = 0;
        for (City city : graph.getVertices().values()) {
            Location loc = city.getLocation();
            if (loc.getX() > maxX) maxX = loc.getX();
            if (loc.getY() > maxY) maxY = loc.getY();
        }
        scaleX = this.getPreferredSize().width / maxX;
        scaleY = this.getPreferredSize().height / maxY;
    }

    private void setupMouseWheelZoom() {
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();
                double factor = Math.exp(-delta); // Calculate zoom factor
                double x = e.getX();
                double y = e.getY();

                // Update transformation matrix for zoom around cursor
                AffineTransform at = new AffineTransform();
                at.translate(x, y);
                at.scale(factor, factor);
                at.translate(-x, -y);

                coordTransform.preConcatenate(at);

                repaint();
            }
        });
    }

    private void setupMousePan() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStartScreen = e.getPoint();
                dragEndScreen = null;
            }

            public void mouseReleased(MouseEvent e) {
                moveCamera(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                moveCamera(e);
            }
        });
    }

    private void moveCamera(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.x - dragStart.x;
            double dy = dragEnd.y - dragStart.y;
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Apply zoom transformation
        AffineTransform transform = new AffineTransform();
        transform.translate(getWidth() / 2.0, getHeight() / 2.0);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        transform.concatenate(coordTransform);

        g2.setTransform(transform);

        // Set font size based on zoomFactor
        int fontSize = (int) Math.max(7, 5 * zoomFactor);  // Base size is 10, adjusted with zoom
        g2.setFont(new Font("Arial", Font.PLAIN, fontSize));

        //Draw grid
        int gridWidth = gridIndex.getWidth();
        int gridHeight = gridIndex.getHeight();

        int[] horizontal_cuts = gridIndex.getHorizontal_cuts();
        int[] vertical_cuts = gridIndex.getVertical_cuts();

        g2.setFont(new Font("Arial", Font.PLAIN, fontSize/3));

        g2.setColor(Color.red);
        for(int i = 0; i < horizontal_cuts.length; i++){
            g2.drawString("" + horizontal_cuts[i],gridWidth,horizontal_cuts[i]);
            g2.drawLine(0,horizontal_cuts[i],gridWidth,horizontal_cuts[i]);
        }
        g2.setColor(Color.blue);
        for(int i = 0; i < vertical_cuts.length; i++){
            g2.drawString("" + vertical_cuts[i],vertical_cuts[i], 0);
            g2.drawLine(vertical_cuts[i],0,vertical_cuts[i],gridHeight);
        }
        g2.setColor(Color.black);
        g2.setFont(new Font("Arial", Font.PLAIN, fontSize));

//        {
        g2.drawLine(0,0,gridWidth,0);
        g2.drawLine(0,gridHeight,gridWidth,gridHeight);
        g2.drawLine(0,0,0,gridHeight);
        g2.drawLine(gridWidth,0,gridWidth,gridHeight);

        //Draw searched cities
        if(citiesSearchedInGraphIndex != null) {
            g2.setColor(new Color(255, 205, 0, 50)); // Blue color with 50% opacity

            g2.fillRect(searchRectangleX1, searchRectangleY1, searchRectangleX2 - searchRectangleX1, searchRectangleY2 - searchRectangleY1); // Corrected width and height calculation

            // Draw dashed rectangle
            float[] dashPattern = {1, 1};
            g2.setColor(Color.orange);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
            g2.drawRect(searchRectangleX1, searchRectangleY1, searchRectangleX2 - searchRectangleX1, searchRectangleY2 - searchRectangleY1);

            g2.setStroke(new BasicStroke());
            g2.setColor(Color.black);
        }

        // Draw vertices
        for (City city : graph.getVertices().values()) {
            Location loc = city.getLocation();
            int x = (int) loc.getX();
            int y = (int) loc.getY();
            int ovalSize = (int) Math.max(2, 1 * zoomFactor);  // Base size is 5, adjusted with zoom

            if(citiesSearchedInGraphIndex != null && x >= searchRectangleX1 && x <= searchRectangleX2 && y >= searchRectangleY1 && y <= searchRectangleY2) {
                g2.setColor(new Color(255, 205, 0)); // Blue color with 50% opacity
            }

            if(city.equals(foundCityByCoordinates)) {
                g2.setColor(Color.cyan);
            }

            // Vertex
            g2.fillOval(x - ovalSize / 2, y - ovalSize / 2, ovalSize, ovalSize);
            // Text
            g2.drawString(city.getName(), x - ovalSize / 2, y - ovalSize / 2);

            g2.setColor(Color.black);
        }


        // Draw edges
        for (EdgeData<String,Road> edge : graph.getEdges()) {
            City c1 = graph.getVertex(edge.getVertex1Key());
            City c2 = graph.getVertex(edge.getVertex2Key());
            if(edge.getData().isAccessible()) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.RED);
            }
            g2.drawLine((int) c1.getLocation().getX(), (int) c1.getLocation().getY(),
                    (int) c2.getLocation().getX(), (int) c2.getLocation().getY());
            // add edge weight from edge.getData().getWeight()
            int midX = ((int) c1.getLocation().getX() + (int) c2.getLocation().getX()) / 2;
            int midY = ((int) c1.getLocation().getY() + (int) c2.getLocation().getY()) / 2;
            g2.drawString(String.valueOf(edge.getData().getWeight()), midX, midY);
        }

        // Draw path in green
        if (path != null && !path.isEmpty()) {
            g2.setColor(Color.GREEN);
            for (int i = 0; i < path.size() - 1; i++) {
                City c1 = graph.getVertex(path.get(i));
                City c2 = graph.getVertex(path.get(i + 1));
                g2.drawLine((int) c1.getLocation().getX(), (int) c1.getLocation().getY(),
                        (int) c2.getLocation().getX(), (int) c2.getLocation().getY());
            }
        }
    }
}