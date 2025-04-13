package main.java.grid;

import main.java.Location;

import javax.imageio.stream.IIOByteBuffer;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GridIndex<T extends LocationInterface> {
    private final int width;
    private final int height;
//    private final ArrayList<T> elements = new ArrayList<>();
    private final ArrayList<String> blocks = new ArrayList<>();
    private final Class<T> Tclass;
    private int[] horizontal_cuts; // z leva do prava
    private int[] vertical_cuts; // z vrchu dolu

    private static final int HORIZONTAL_CUT = 0;
    private static final int VERTICAL_CUT = 1;

    private int lastCut = HORIZONTAL_CUT;

    private T[][] grid_address;

    private static final int MAX_STRING_LENGTH_IN_BLOCK = 30;

    private final RandomAccessFile file;

    private static class GridAddressIndexes {
        int x;
        int y;

        public GridAddressIndexes(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public GridIndex(int width, int height, int blockFactor, Class<T> clazz) throws IOException {
        this.width = width;
        this.height = height;
        this.horizontal_cuts = new int[]{0, height};
        this.vertical_cuts = new int[]{0, width};
        this.Tclass = clazz;
        this.grid_address = (T[][]) Array.newInstance(clazz, 1, 1);
        File createdFile = this.initFile();
        this.file = new RandomAccessFile(createdFile, "rw");
        System.out.println("fields: "+clazz.getDeclaredFields().length);
        for(int i = 0; i < clazz.getDeclaredFields().length; i++){
            System.out.println(clazz.getDeclaredFields()[i].getType());
        }
        this.createControlBlock(blockFactor);
        //TODO:
        // spočítám velikost bloku! String - každá char 2 byty  délka bude 30, int 4 byty
        // blockFactor - blokovací faktor předám konstruktorem?
        // blockSize - vypočítám blockSize -> blokovacíFaktor * velikost T? Nvm
        // blockCount - nastavím počet bloků na 1? Nebo na 0 protože se řídící nebude počítat?
        // uloží řídící blok
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getHorizontal_cuts() {
        return horizontal_cuts;
    }

    public int[] getVertical_cuts() {
        return vertical_cuts;
    }

    public void add(T element) throws IllegalArgumentException {
        if (element.getLocation().getX() >= width || element.getLocation().getY() >= height) {
            throw new IllegalArgumentException("Element is out of bounds");
        }

        if (isLocationOccupied(element.getLocation())) {
            System.out.println("Location is ocupaied, throwing exception");
            throw new IllegalArgumentException("Location is already occupied by another element or a cut. Element:" + element.toString());
        }

        if (shouldPerformCut(element)) {
            cut(element);
        }

//        elements.add(element);
        this.saveElementToBlock(element);
        mapAllCitiesToGridAddress();

        printGrid();
    }

    private File initFile(){
        String fileName = "gridIndex-"+ UUID.randomUUID() +".bin";
        try {
            File myObj = new File(fileName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            return myObj;
        } catch (IOException e) {
            System.out.println("An error occurred.");
            throw new RuntimeException(e);
        }
    }

    private void createControlBlock(int blockFactor) throws IOException {
        int recordSize = calculateRecordSize(this.Tclass);
        int blockLength = recordSize * blockFactor;

        // Přejdi na začátek souboru
        this.file.seek(0);

        // Zápis řídicího bloku (zatím jednoduché metadata)
        this.file.writeInt(recordSize);      // 4 B
        this.file.writeInt(blockFactor);     // 4 B
        this.file.writeInt(blockLength);     // 4 B
        this.file.writeInt(0);               // 4 B – počet datových bloků (zatím 0)

        // Celkem zapsáno: 16 bajtů
        int metadataSize = 16;

        // Vyplníme zbytek řídicího bloku nulami
        int paddingSize = blockLength - metadataSize;
        byte[] padding = new byte[paddingSize];
        this.file.write(padding);

        System.out.println("Record size: " + recordSize);
        System.out.println("Block size: " + blockLength);
    }


    private int calculateRecordSize(Class<?> clazz) {
        int totalSize = 0;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            totalSize += getFieldSize(field);
        }
        return totalSize;
    }


    private int getFieldSize(Field field) {
        Class<?> type = field.getType();

        if (type == int.class) return 4;
        if (type == double.class) return 8;
        if (type == long.class) return 8;
        if (type == float.class) return 4;
        if (type == short.class) return 2;
        if (type == byte.class) return 1;
        if (type == boolean.class) return 1;

        if (type == char.class) return 2;

        if (type == String.class) {
            return 2 + MAX_STRING_LENGTH_IN_BLOCK * 2; // 2 bajty pro délku + 30 znaků * 2 bajty
        }

        // Rekurzivně – pro vnořené objekty
        Field[] nestedFields = type.getDeclaredFields();
        int total = 0;
        for (Field f : nestedFields) {
            total += getFieldSize(f);
        }
        return total;
    }

    private int readRecordSize() throws IOException {
        this.file.seek(0);
        return this.file.readInt();
    }

    private int readBlockFactor() throws IOException {
        this.file.seek(4);
        return this.file.readInt();
    }

    private int readBlockLength() throws IOException {
        this.file.seek(8);
        return this.file.readInt();
    }

    private int readNumberOfBlocks() throws IOException {
        this.file.seek(12);
        return this.file.readInt();
    }


    private void saveElementToBlock(T element){
        //TODO: uloží blok s novým městem do souboru
    }

    private void readBlockFromFile(){
        // TODO: Co bude vracet? List? Pomocnou třídu blok? Nvm.
    }

    private void saveBlockToFile(List<T> block){
        //TODO: Uloží blok do souboru

    }

    private void printGrid() {
        System.out.println("x: " + Arrays.toString(vertical_cuts));
        System.out.println("y: " + Arrays.toString(horizontal_cuts));


        for (int j = 0; j < grid_address[0].length; j++) {
            for (int i = 0; i < grid_address.length; i++) {
                if (grid_address[i][j] != null) {
                    System.out.print(grid_address[i][j].toString() + " ");
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
    }

    private boolean isLocationOccupied(Location location) {
        for (T[] row : grid_address) {
            for (T element : row) {
                if (element != null && (Math.abs(element.getLocation().getX() - location.getX()) <= 0 && Math.abs(element.getLocation().getY() - location.getY()) <= 0)) {
//                    System.out.println("City: " + element.getName() + " is on the same location as new city: " + location.toString());
                    return true;
                }
            }
        }
        for (int cut : horizontal_cuts) {
            if (Math.abs(cut - location.getY()) <= 0) {
                System.out.println("Horizontal cut is on this location: " + cut);
                return true;
            }
        }
        for (int cut : vertical_cuts) {
            if (Math.abs(cut - location.getX()) <= 0) {
                System.out.println("Vertical cut is on this location: " + cut);
                return true;
            }
        }
        return false;
    }

    public T findElementsByCoordinates(int x, int y) throws IllegalArgumentException {
        T foundElement = null;
//        for (T element : elements) {
//            if (element.getLocation().getX() == x && element.getLocation().getY() == y) {
//                foundElement = element;
//                break;
//            }
//        }
        return foundElement;
    }

    public ArrayList<T> findElementBySegment(int x1, int y1, int x2, int y2) {

        if (x1 < 0 || x1 > width || y1 < 0 || y1 > height || x2 < 0 || x2 > width || y2 < 0 || y2 > height) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }

        int xStartIndex = -1;
        int xEndIndex = -1;
        for (int i = 0; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i] > x1) {
                xStartIndex = i - 1;
            }
            if (vertical_cuts[i] >= x2) {
                xEndIndex = i;
            }
        }

        int yStartIndex = -1;
        int yEndIndex = -1;

        for (int i = 0; i < horizontal_cuts.length; i++) {
            if (horizontal_cuts[i] > y1) {
                yStartIndex = i - 1;
            }
            if (horizontal_cuts[i] >= y2) {
                yEndIndex = i;
            }
        }

        ArrayList<T> elements = new ArrayList<>();

        for (int i = xStartIndex; i < xEndIndex; i++) {
            for (int j = yStartIndex; j < yEndIndex; j++) {
                if (grid_address[i][j] != null && isElementInSearchDimensions(grid_address[i][j], x1, y1, x2, y2)) {
                    elements.add(grid_address[i][j]);
                }
            }
        }

        return elements;
    }

    private boolean isElementInSearchDimensions(T element, int x1, int y1, int x2, int y2) {
        return element.getLocation().getX() >= x1 && element.getLocation().getY() >= y1 && element.getLocation().getX() <= x2 && element.getLocation().getY() <= y2;
    }

    private boolean shouldPerformCut(T element) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(element);
        return grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] != null;
    }

    private GridAddressIndexes getElementGridAddressIndexes(T element) {
        Location elementLocation = element.getLocation();
        int xIndex = getElementXIndexInGridAddress(elementLocation.getX());
        int yIndex = getElementYIndexInGridAddress(elementLocation.getY());
        return new GridAddressIndexes(xIndex, yIndex);
    }

    private int getElementXIndexInGridAddress(int elementX) {
        for (int i = 0; i < vertical_cuts.length; i++) {
            if (elementX < vertical_cuts[i]) {
                return i - 1;
            }
        }
        return -1;
    }

    private int getElementYIndexInGridAddress(int elementY) {
        for (int i = 0; i < horizontal_cuts.length; i++) {
            if (elementY < horizontal_cuts[i]) {
                return i - 1;
            }
        }
        return -1;
    }

    private void cut(T element) {
        if (lastCut == HORIZONTAL_CUT) {
            // střídám, takže chci řezat druhým směrem
            boolean cutSuccessful = performVerticalCut(element);
            if (!cutSuccessful) { // pokud to nejde tak zkusím horizontálně
                boolean horizontalCutSuccessful = performHorizontalCut(element);
                if (!horizontalCutSuccessful) // pokud nejde ani horizonrálně, tak výjmka
                    throw new IllegalArgumentException("Cannot cut the grid");
            } else {
                lastCut = VERTICAL_CUT;
            }
        } else {
            boolean horizontalCutSuccessful = performHorizontalCut(element);
            if (!horizontalCutSuccessful) {
                boolean verticalCutSuccessful = performVerticalCut(element);
                if (!verticalCutSuccessful)
                    throw new IllegalArgumentException("Cannot cut the grid");
            } else {
                lastCut = HORIZONTAL_CUT;
            }
        }
    }

    private boolean canAddToInBetween(int elementInGridPosition, int newElementPosition, int inBetween) {
        if (Math.abs(elementInGridPosition - newElementPosition) > 1)
            if (inBetween < elementInGridPosition && inBetween > newElementPosition)
                return (Math.abs(elementInGridPosition - inBetween) > 1);
        if ((inBetween > elementInGridPosition && inBetween < newElementPosition))
            return Math.abs(newElementPosition - inBetween) > 1;
        return false;
    }

    private boolean canRetrieveFromInBetween(int elementInGridPosition, int newElementPosition, int inBetween) {
        if (Math.abs(elementInGridPosition - newElementPosition) > 1)
            if (inBetween < elementInGridPosition && inBetween > newElementPosition)
                return (Math.abs(newElementPosition - inBetween) > 1);
        if ((inBetween > elementInGridPosition && inBetween < newElementPosition))
            return Math.abs(elementInGridPosition - inBetween) > 1;
        return false;
    }

    private boolean performHorizontalCut(T newElement) {

        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);
        T elementInGrid = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        int inBetween = Math.round((elementInGrid.getLocation().getY() + newElement.getLocation().getY()) / 2.0f);

//        boolean didChangeInBetween = false;
//
//        do {
//            didChangeInBetween = false;
//            // Check if inBetween intersects with any existing city
//            for (T element : elements) {
//                if (element != null && element.getLocation().getY() == inBetween) {
//
//                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
//                    if (canAddToInBetween(elementInGrid.getLocation().getY(), newElement.getLocation().getY(), inBetween)) {
//                        inBetween++;
//                        didChangeInBetween = true;
//                        break;
//                    } else if (canRetrieveFromInBetween(elementInGrid.getLocation().getY(), newElement.getLocation().getY(), inBetween)) {
//                        inBetween--;
//                        didChangeInBetween = true;
//                        break;
//                    } else {
//                        return false;
//                    }
//
//                }
//            }
//        } while (didChangeInBetween);

//        System.out.println("Performing new vertical cut at y: " + inBetween + " between " + elementInGrid.getName() + " y: " + elementInGrid.getLocation().getY() + " and " + newElement.getName() + " y: " + newElement.getLocation().getY());

        int[] newHorizontalCuts = new int[horizontal_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < horizontal_cuts.length; i++) {
            if (horizontal_cuts[i - 1] < inBetween && horizontal_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }

        // nakopíruju pole po index nového řezu
        System.arraycopy(horizontal_cuts, 0, newHorizontalCuts, 0, indexForInBetween);
        newHorizontalCuts[indexForInBetween] = inBetween;
        // nakopíruju pole od nového řezu dál
        System.arraycopy(horizontal_cuts, indexForInBetween, newHorizontalCuts, indexForInBetween + 1, horizontal_cuts.length - indexForInBetween);
        horizontal_cuts = newHorizontalCuts;

        // vytvořím si nové pole s o jedna delšími sloupci
        grid_address = (T[][]) Array.newInstance(Tclass, grid_address.length, grid_address[0].length + 1);
        return true;
    }

    private boolean performVerticalCut(T newElement) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);
        T elementInGrid = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        int inBetween = Math.round((elementInGrid.getLocation().getX() + newElement.getLocation().getX()) / 2.0f);

//        boolean didChangeInBetween = false;
//        do {
//            didChangeInBetween = false;
//            // Check if inBetween intersects with any existing city
//            for (T existingElement : elements) {
//                if (existingElement != null && existingElement.getLocation().getX() == inBetween) {
//
//                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
//                    if (canAddToInBetween(elementInGrid.getLocation().getX(), newElement.getLocation().getX(), inBetween)) {
//                        inBetween++;
//                        didChangeInBetween = true;
//                        break;
//                    } else if (canRetrieveFromInBetween(elementInGrid.getLocation().getX(), newElement.getLocation().getX(), inBetween)) {
//                        inBetween--;
//                        didChangeInBetween = true;
//                        break;
//                    } else {
//                        return false;
//                    }
//                }
//            }
//        } while (didChangeInBetween);


//        System.out.println("Performing new vertical cut at x: " + inBetween + " between " + elementInGrid.getName() + " x: " + elementInGrid.getLocation().getX() + " and " + newElement.getName() + " x: " + newElement.getLocation().getX());

        int[] newVerticalCuts = new int[vertical_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i - 1] < inBetween && vertical_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }
        System.arraycopy(vertical_cuts, 0, newVerticalCuts, 0, indexForInBetween);
        newVerticalCuts[indexForInBetween] = inBetween;
        System.arraycopy(vertical_cuts, indexForInBetween, newVerticalCuts, indexForInBetween + 1, vertical_cuts.length - indexForInBetween);
        vertical_cuts = newVerticalCuts;

        grid_address = (T[][]) Array.newInstance(Tclass, grid_address.length + 1, grid_address[0].length);
        return true;
    }

    private void mapAllCitiesToGridAddress() {
        // grid_address naplnit null
        for (int i = 0; i < grid_address.length; i++) {
            for (int j = 0; j < grid_address[0].length; j++) {
                grid_address[i][j] = null;
            }
        }
        // pro každé město v grafu, vložit na správné místo v grid_address
//        for (T element : elements) {
//            addElementToGridAddress(element);
//        }
    }

    private void addElementToGridAddress(T element) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(element);
        if (grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] != null) {
            throw new IllegalArgumentException("Index is already in use!");
        }
        grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] = element;
    }

}
