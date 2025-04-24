package main.java.grid;

import main.java.Location;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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

    private int[][] grid_address;

    private static final int MAX_STRING_LENGTH_IN_BLOCK = 30;

    private RandomAccessFile file;
    private String fileName;

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
        this.grid_address = new int[1][1];
        File createdFile = this.initFile();
        this.file = new RandomAccessFile(createdFile, "rw");
        System.out.println("fields: " + clazz.getDeclaredFields().length);
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            System.out.println(clazz.getDeclaredFields()[i].getType());
        }
        this.createControlBlock(blockFactor);
        System.out.println("Number of blocks: " + this.readNumberOfBlocks());
        int blockNumber = this.addNewBlock();
        System.out.println("Number of blocks: " + this.readNumberOfBlocks());
        this.grid_address[0][0] = blockNumber;
    }

    public void saveToFile(String filename) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(grid_address);
        out.writeObject(horizontal_cuts);
        out.writeObject(vertical_cuts);
        out.writeObject(fileName); // Save the file path instead of file descriptor
    }

    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        this.grid_address = (int[][]) in.readObject();
        this.horizontal_cuts = (int[]) in.readObject();
        this.vertical_cuts = (int[]) in.readObject();
        this.fileName = (String) in.readObject();
        File fileToOpen = new File(fileName);
        if (!fileToOpen.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }
        this.file = new RandomAccessFile(fileToOpen, "rw");
        in.close();
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

//    public T read() {
//        try {
//            int recordSize = readRecordSize();
//            int blockLength = readBlockLength();
//            int blockFactor = readBlockFactor();
//            long blockOffset = blockLength; // blok 1, protože blok 0 = řídicí
//
//            for (int i = 0; i < blockFactor; i++) {
//                long recordOffset = blockOffset + (long) i * recordSize;
//                this.file.seek(recordOffset);
//                byte valid = this.file.readByte();
//
//                if (valid == 1) {
//                    byte[] recordBytes = new byte[recordSize - 1];
//                    this.file.readFully(recordBytes);
//                    return deserializeRecord(recordBytes, 0); // offset = 0, protože valid už jsme přečetli
//                }
//            }
//
//            return null; // žádný platný záznam v prvním bloku
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    public void add(T element) throws IllegalArgumentException, IOException {
        if (element.getLocation().getX() >= width || element.getLocation().getY() >= height) {
            throw new IllegalArgumentException("Element is out of bounds");
        }

        if (isLocationOccupied(element.getLocation())) {
            System.out.println("Location is ocupaied, throwing exception");
            throw new IllegalArgumentException("Location is already occupied by another element or a cut. Element:" + element.toString());
        }
        System.out.println("Number of blocks::: " + this.readNumberOfBlocks());

        if (shouldPerformCut(element)) {
            cut(element);
//            long blockNumber = addNewBlock();
//            this.saveElementToBlock(element, blockNumber); // řezal jsem takže přidávám element do nového bloku
        } else {
            GridAddressIndexes indexes = this.getElementGridAddressIndexes(element);
            this.saveElementToBlock(element, this.grid_address[indexes.x][indexes.y]);
        }

        System.out.println("Number of blocks::: " + this.readNumberOfBlocks());


//        mapAllBlocksToGridAddress();

        printGrid();
    }

    public List<T> readAllElements() throws IOException {
        int numberOfBlocks = readNumberOfBlocks();
        List<T> allElements = new ArrayList<>();

        for (int block = 1; block <= numberOfBlocks; block++) {
            List<T> blockElements = readBlockFromFile(block);
            for (T element : blockElements) {
                if (element != null) {
                    allElements.add(element);
                }
            }
        }

        return allElements;
    }


    private File initFile() {
        String fileName = "gridIndex-" + UUID.randomUUID() + ".bin";
        this.fileName = fileName;
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
        this.file.writeInt(0); // 4 B - počet entit
        // Celkem zapsáno: 16 bajtů
        int metadataSize = 20;

        // Vyplníme zbytek řídicího bloku nulami
        int paddingSize = blockLength - metadataSize;
        byte[] padding = new byte[paddingSize];
        this.file.write(padding);

        System.out.println("Record size: " + recordSize);
        System.out.println("Block size: " + blockLength);
    }

    private int addNewBlock() throws IOException {
        int blockLength = readBlockLength();
        int numberOfBlocks = readNumberOfBlocks();

        // Spočítat offset pro nový blok
        long newBlockOffset = (long) (numberOfBlocks + 1) * blockLength;

        // Seek na konec a zapsat prázdný blok
        this.file.seek(newBlockOffset);
        byte[] emptyBlock = new byte[blockLength];
        this.file.write(emptyBlock);

        // Zvýšit a zapsat nový počet bloků
        updateNumberOfBlocks(numberOfBlocks + 1);

        return numberOfBlocks + 1; // vrací číslo nového bloku
    }

    private void clearBlock(int blockNumber) throws IOException {
        int blockLength = readBlockLength();
        long blockOffset = (long) blockNumber * blockLength;

        this.file.seek(blockOffset);
        byte[] empty = new byte[blockLength];
        this.file.write(empty);
    }


    private int calculateRecordSize(Class<?> clazz) {
        int totalSize = 0;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            totalSize += getFieldSize(field);
        }
        totalSize = totalSize + 1; // 1 byte jako flag, zda je tam zapsané město.
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

    private void updateNumberOfBlocks(int numberOfBlocks) throws IOException {
        this.file.seek(12);
        this.file.writeInt(numberOfBlocks);
    }


    private void saveElementToBlock(T element, long blockNumber) throws IOException {
        int recordSize = readRecordSize();
        int blockLength = readBlockLength();
        int blockFactor = readBlockFactor();

        // spočítej začátek bloku v souboru
        long blockOffset = blockNumber * blockLength;

        System.out.println("🔧 DEBUG: recordSize = " + recordSize);
        System.out.println("🔧 DEBUG: blockLength = " + blockLength);
        System.out.println("🔧 DEBUG: blockFactor = " + blockFactor);
        System.out.println("🔧 DEBUG: Calculated blockOffset = " + blockOffset);

        // projdi jednotlivé pozice v bloku a najdi první volnou
        for (int i = 0; i < blockFactor; i++) {
            long recordOffset = blockOffset + (long) i * recordSize;
            this.file.seek(recordOffset);
            byte valid = this.file.readByte();

            if (valid == 0) {
                // místo je volné → zapíšeme sem
                byte[] serializedRecord = serializeRecord(element);
                this.file.seek(recordOffset);
                this.file.write(serializedRecord);
                return;
            }
        }

        throw new IOException("Blok " + blockNumber + " je plný.");
    }


    private List<T> readBlockFromFile(long blockNumber) throws IOException {
        int recordSize = readRecordSize();
        int blockLength = readBlockLength();
        int blockFactor = readBlockFactor();
        long blockOffset = blockNumber * blockLength;

        List<T> records = new ArrayList<>(blockFactor);

        for (int i = 0; i < blockFactor; i++) {
            long recordOffset = blockOffset + (long) i * recordSize;
            this.file.seek(recordOffset);
            byte valid = this.file.readByte();

            if (valid == 1) {
                byte[] recordBytes = new byte[recordSize - 1];
                this.file.readFully(recordBytes);
                T element = deserializeRecord(recordBytes, 0); // offset 0, protože jsme valid už přečetli
                records.add(element);
            }
        }

        return records;
    }


    private void saveBlockToFile(List<T> block) {
        //TODO: Uloží blok do souboru

    }

    private byte[] serializeRecord(T obj) throws IOException {
        try {
            int recordSize = readRecordSize(); // z řídicího bloku
            ByteBuffer buffer = ByteBuffer.allocate(recordSize);
            buffer.order(ByteOrder.BIG_ENDIAN);

            // 1. valid = 1 (první bajt)
            buffer.put((byte) 1);

            // 2. Zbytek: obsah atributů objektu T
            Field[] fields = Tclass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                Class<?> type = field.getType();

                if (type == int.class) {
                    buffer.putInt((int) value);
                } else if (type == String.class) {
                    String str = (String) value;
                    byte[] strBytes = str.getBytes(StandardCharsets.UTF_16BE);
                    byte[] fixedLength = new byte[60]; // max 30 znaků (2 bajty/znak)
                    System.arraycopy(strBytes, 0, fixedLength, 0, Math.min(strBytes.length, 60));
                    buffer.put(fixedLength);
                } else {
                    // Vnořený objekt
                    Field[] nestedFields = type.getDeclaredFields();
                    for (Field nested : nestedFields) {
                        nested.setAccessible(true);
                        Object nestedValue = nested.get(value);
                        Class<?> nestedType = nested.getType();

                        if (nestedType == int.class) {
                            buffer.putInt((int) nestedValue);
                        } else if (nestedType == String.class) {
                            String str = (String) nestedValue;
                            byte[] strBytes = str.getBytes(StandardCharsets.UTF_16BE);
                            byte[] fixedLength = new byte[60];
                            System.arraycopy(strBytes, 0, fixedLength, 0, Math.min(strBytes.length, 60));
                            buffer.put(fixedLength);
                        } else {
                            throw new IOException("Nepodporovaný typ ve vnořeném objektu: " + nestedType);
                        }
                    }
                }
            }

            return buffer.array();

        } catch (Exception e) {
            throw new IOException("Chyba při serializaci objektu", e);
        }
    }


    private T deserializeRecord(byte[] data, int offset) throws IOException {
        try {
            T instance = Tclass.getDeclaredConstructor().newInstance();
            Field[] fields = Tclass.getDeclaredFields();

            ByteBuffer buffer = ByteBuffer.wrap(data, offset, data.length - offset);
            buffer.order(ByteOrder.BIG_ENDIAN);

            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> type = field.getType();

                if (type == int.class) {
                    field.setInt(instance, buffer.getInt());
                } else if (type == String.class) {
                    byte[] strBytes = new byte[60]; // 30 znaků UTF-16
                    buffer.get(strBytes);
                    String str = new String(strBytes, StandardCharsets.UTF_16BE).trim();
                    field.set(instance, str);
                } else {
                    // Předpokládáme, že je to vnořený objekt
                    Object nestedObj = type.getDeclaredConstructor().newInstance();
                    Field[] nestedFields = type.getDeclaredFields();

                    for (Field nestedField : nestedFields) {
                        nestedField.setAccessible(true);
                        Class<?> nestedType = nestedField.getType();

                        if (nestedType == int.class) {
                            nestedField.setInt(nestedObj, buffer.getInt());
                        } else if (nestedType == String.class) {
                            byte[] nestedStrBytes = new byte[60];
                            buffer.get(nestedStrBytes);
                            String nestedStr = new String(nestedStrBytes, StandardCharsets.UTF_16BE).trim();
                            nestedField.set(nestedObj, nestedStr);
                        } else {
                            throw new IOException("Nepodporovaný typ ve vnořeném objektu: " + nestedType);
                        }
                    }

                    field.set(instance, nestedObj);
                }
            }

            return instance;

        } catch (Exception e) {
            throw new IOException("Chyba při deserializaci záznamu", e);
        }
    }


    private void printGrid() {
        System.out.println("x: " + Arrays.toString(vertical_cuts));
        System.out.println("y: " + Arrays.toString(horizontal_cuts));


        for (int j = 0; j < grid_address[0].length; j++) {
            for (int i = 0; i < grid_address.length; i++) {
                if (grid_address[i][j] != 0) {
                    System.out.print(grid_address[i][j] + " ");
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
    }

    private boolean isLocationOccupied(Location location) {
        for (int[] row : grid_address) {
            for (int blockNumber : row) {
                if (blockNumber != 0) {
                    try {
                        List<T> block = readBlockFromFile(blockNumber);
                        for (T element : block) {
                            if (element != null && element.getLocation().getX() == location.getX() && element.getLocation().getY() == location.getY()) {
                                System.out.println("City: " + element + " is on the same location as new city: " + location.toString());
                                return true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                if (element != null && (Math.abs(element.getLocation().getX() - location.getX()) <= 0 && Math.abs(element.getLocation().getY() - location.getY()) <= 0)) {
//                    System.out.println("City: " + element.getName() + " is on the same location as new city: " + location.toString());
//                    return true;
//                }
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

//        if (x1 < 0 || x1 > width || y1 < 0 || y1 > height || x2 < 0 || x2 > width || y2 < 0 || y2 > height) {
//            throw new IllegalArgumentException("Coordinates are out of bounds");
//        }
//
//        int xStartIndex = -1;
//        int xEndIndex = -1;
//        for (int i = 0; i < vertical_cuts.length; i++) {
//            if (vertical_cuts[i] > x1) {
//                xStartIndex = i - 1;
//            }
//            if (vertical_cuts[i] >= x2) {
//                xEndIndex = i;
//            }
//        }
//
//        int yStartIndex = -1;
//        int yEndIndex = -1;
//
//        for (int i = 0; i < horizontal_cuts.length; i++) {
//            if (horizontal_cuts[i] > y1) {
//                yStartIndex = i - 1;
//            }
//            if (horizontal_cuts[i] >= y2) {
//                yEndIndex = i;
//            }
//        }
//
//        ArrayList<T> elements = new ArrayList<>();
//
//        for (int i = xStartIndex; i < xEndIndex; i++) {
//            for (int j = yStartIndex; j < yEndIndex; j++) {
//                if (grid_address[i][j] != null && isElementInSearchDimensions(grid_address[i][j], x1, y1, x2, y2)) {
//                    elements.add(grid_address[i][j]);
//                }
//            }
//        }
//
//        return elements;
        return new ArrayList<>();
    }

    private boolean isElementInSearchDimensions(T element, int x1, int y1, int x2, int y2) {
        return element.getLocation().getX() >= x1 && element.getLocation().getY() >= y1 && element.getLocation().getX() <= x2 && element.getLocation().getY() <= y2;
    }

    private boolean shouldPerformCut(T element) throws IOException {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(element);
        List<T> elements = this.readBlockFromFile(grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y]);
        return elements.size() == this.readBlockFactor();
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

    private void cut(T element) throws IOException {
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

    private boolean performHorizontalCut(T newElement) throws IOException {

        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);

        int blockNumber = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        List<T> elements = this.readBlockFromFile(blockNumber);

        T closestElement = elements.getFirst();
        for (T element : elements) {
            if (element.getLocation().getY() < closestElement.getLocation().getY()) {
                closestElement = element;
            }
        }

        int inBetween = Math.round((closestElement.getLocation().getY() + newElement.getLocation().getY()) / 2.0f);

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

        // 1. Vytvoření nového bloku
        int newBlockNumber = this.addNewBlock();
        boolean insertAbove = newElement.getLocation().getY() < closestElement.getLocation().getY();

// 2. Přesun měst ze starého bloku do nového
        List<T> originalBlockElements = new ArrayList<>(elements); // záloha pro přepis
        originalBlockElements.add(newElement);
        List<T> remainingInOldBlock = new ArrayList<>();
        List<T> movedToNewBlock = new ArrayList<>();

        for (T element : originalBlockElements) {
            if (insertAbove) {
                // nový blok jde nahoru → nahoru = Y < inBetween
                if (element.getLocation().getY() < inBetween) {
                    movedToNewBlock.add(element);
                } else {
                    remainingInOldBlock.add(element);
                }
            } else {
                // nový blok jde dolů → dolů = Y > inBetween
                if (element.getLocation().getY() > inBetween) {
                    movedToNewBlock.add(element);
                } else {
                    remainingInOldBlock.add(element);
                }
            }
        }

// 3. Vyčistit původní blok a zapsat zpět jen ty, co zůstávají
        this.clearBlock(blockNumber);
        for (T e : remainingInOldBlock) {
            this.saveElementToBlock(e, blockNumber);
        }

// 4. Zapsat nové prvky do nového bloku
        for (T e : movedToNewBlock) {
            this.saveElementToBlock(e, newBlockNumber);
        }

        this.applyHorizontalSplitToGrid(indexForInBetween, blockNumber, newBlockNumber, insertAbove);

        return true;
    }

    private boolean performVerticalCut(T newElement) throws IOException {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);
        int blockNumber = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        List<T> elements = this.readBlockFromFile(blockNumber);

        T closestElement = null;
        int minDistance = Integer.MAX_VALUE;
        int newX = newElement.getLocation().getX();

        for (T element : elements) {
            if (element == null) continue;
            int existingX = element.getLocation().getX();
            int distance = Math.abs(existingX - newX);
            if (distance < minDistance) {
                minDistance = distance;
                closestElement = element;
            }
        }

        int inBetween = Math.round((closestElement.getLocation().getX() + newX) / 2.0f);

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

        // 1. Vytvoření nového bloku
        int newBlockNumber = this.addNewBlock();
        boolean insertLeft = newElement.getLocation().getX() < closestElement.getLocation().getX();

// 2. Přesun měst ze starého bloku do nového
        List<T> originalBlockElements = new ArrayList<>(elements); // záloha pro přepis
        originalBlockElements.add(newElement);
        List<T> remainingInOldBlock = new ArrayList<>();
        List<T> movedToNewBlock = new ArrayList<>();

        for (T element : originalBlockElements) {
            if (insertLeft) {
                // nový blok jde vlevo → vlevo = X < inBetween
                if (element.getLocation().getX() < inBetween) {
                    movedToNewBlock.add(element);
                } else {
                    remainingInOldBlock.add(element);
                }
            } else {
                // nový blok jde doprava → doprava = X > inBetween
                if (element.getLocation().getX() > inBetween) {
                    movedToNewBlock.add(element);
                } else {
                    remainingInOldBlock.add(element);
                }
            }
        }

// 3. Vyčistit původní blok a zapsat zpět jen ty, co zůstávají
        this.clearBlock(blockNumber);
        for (T e : remainingInOldBlock) {
            this.saveElementToBlock(e, blockNumber);
        }

// 4. Zapsat nové prvky do nového bloku
        for (T e : movedToNewBlock) {
            this.saveElementToBlock(e, newBlockNumber);
        }

        this.applyVerticalSplitToGrid(indexForInBetween, blockNumber, newBlockNumber, insertLeft);

        return true;
    }

    private void applyHorizontalSplitToGrid(int indexForInBetween, int blockNumber, int newBlockNumber, boolean insertAbove) {
        int[][] originalGrid = grid_address;
        int columns = originalGrid.length;
        int originalRows = originalGrid[0].length;

        // Vytvoř nový grid s o 1 větším počtem řádků
        int[][] newGrid = new int[columns][originalRows + 1];

        for (int col = 0; col < columns; col++) {
            // Zkopíruj řádky s posunem
            for (int row = 0; row < indexForInBetween; row++) {
                newGrid[col][row] = originalGrid[col][row];
            }
            for (int row = indexForInBetween; row < originalRows; row++) {
                newGrid[col][row + 1] = originalGrid[col][row];
            }

            // Rozdělení nebo zachování
            if (originalGrid[col][indexForInBetween - 1] == blockNumber) {
                if (insertAbove) {
                    newGrid[col][indexForInBetween - 1] = newBlockNumber;
                    newGrid[col][indexForInBetween] = blockNumber;
                } else {
                    newGrid[col][indexForInBetween - 1] = blockNumber;
                    newGrid[col][indexForInBetween] = newBlockNumber;
                }
            } else {
                // ⬅️ DŮLEŽITÉ: blok, který se nerozděluje, zkopíruj do nové řádky
                newGrid[col][indexForInBetween] = originalGrid[col][indexForInBetween - 1];
            }
        }


        // Aktualizuj grid
        grid_address = newGrid;
    }


    private void applyVerticalSplitToGrid(int indexForInBetween, int blockNumber, int newBlockNumber, boolean insertLeft) {
        int[][] originalGrid = grid_address;
        int originalCols = originalGrid.length;
        int rows = originalGrid[0].length;

        // Vytvoř nový grid o jeden širší (víc sloupců)
        int[][] newGrid = new int[originalCols + 1][rows];

        // Pro každý řádek
        for (int row = 0; row < rows; row++) {
            // Zkopíruj původní sloupce do nového gridu, s posunem za řezem
            for (int col = 0; col < indexForInBetween; col++) {
                newGrid[col][row] = originalGrid[col][row];
            }
            for (int col = indexForInBetween; col < originalCols; col++) {
                newGrid[col + 1][row] = originalGrid[col][row];
            }

            // Rozdělení nebo zachování
            if (originalGrid[indexForInBetween - 1][row] == blockNumber) {
                if (insertLeft) {
                    newGrid[indexForInBetween - 1][row] = newBlockNumber;
                    newGrid[indexForInBetween][row] = blockNumber;
                } else {
                    newGrid[indexForInBetween - 1][row] = blockNumber;
                    newGrid[indexForInBetween][row] = newBlockNumber;
                }
            } else {
                // ⬅️ DŮLEŽITÉ: blok, který se nerozděluje, zkopíruj do nové pozice
                newGrid[indexForInBetween][row] = originalGrid[indexForInBetween - 1][row];
            }
        }

        // Aktualizuj grid
        grid_address = newGrid;
    }

}