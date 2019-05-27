package com.gdx.cellular;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.NeighborLocation;
import com.gdx.cellular.elements.OutOfBoundsCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CellularMatrix {

    public int innerArraySize;
    public int outerArraySize;
    public int pixelSizeModifier;
    private List<Integer> shuffledXIndexes;
    private List<List<Integer>> shuffledXIndexesForThreads;
    private int threadedIndexOffset = 0;

    private Array<Array<Cell>> matrix;
    private Array<Spout> spoutArray;

    public CellularMatrix(int width, int height, int pixelSizeModifier) {
        this.pixelSizeModifier = pixelSizeModifier;
        innerArraySize = toMatrix(width);
        outerArraySize = toMatrix(height);
        matrix = generateMatrix();
        linkNeighbors(matrix);
        shuffledXIndexes = generateShuffledIndexes(innerArraySize);

        calculateAndSetThreadedXIndexOffset();
        spoutArray = new Array<>();
    }

    public void calculateAndSetThreadedXIndexOffset() {
        if (shuffledXIndexesForThreads != null) {
            threadedIndexOffset = (int) (Math.random() * (innerArraySize / shuffledXIndexesForThreads.size()));
        } else {
            threadedIndexOffset = 0;
        }
    }

    private Array<Array<Cell>> generateMatrix() {
        Array<Array<Cell>> outerArray = new Array<>(true, outerArraySize);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Cell> innerArr = new Array<>(true, innerArraySize);
            for (int x = 0; x < innerArraySize; x++) {
                innerArr.add(new Cell(new Vector3(x, y, 0), new Vector3(toPixel(x), toPixel(y), 0)));
            }
            outerArray.add(innerArr);
        }
        return outerArray;
    }

    private void linkNeighbors(Array<Array<Cell>> matrix) {
        for (Array<Cell> row : matrix) {
            for (Cell cell : row) {
                for (int x = -1; x <=1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (isWithinBounds(cell.matrixLocation.x + x, cell.matrixLocation.y + y) &&
                                !(x == 0 && y == 0)) {
                            Cell neighbor = get(cell.matrixLocation.x + x, cell.matrixLocation.y + y);
                            cell.setNeighbor(NeighborLocation.fromInts(x, y), neighbor);
                        } else if (!(x == 0 && y == 0)) {
                            cell.setNeighbor(NeighborLocation.fromInts(x, y), new OutOfBoundsCell());
                        }
                    }
                }
            }
        }
    }



    public void stepAndDrawAll(ShapeRenderer sr) {
        stepAll();
        drawAll(sr);
    }

    private void stepAll() {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Cell> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x).getElement();
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void drawAll(ShapeRenderer sr) {
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Cell> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x).getElement();
                if (element == null) {
//                    sr.setColor(Color.CLEAR);
                    continue;
                }
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    Element nextElement = get(following, y).getElement();
                    if (nextElement == null || nextElement.color != currentColor) {
                        break;
                    }
                    toIndex = following;
                }
                if (element != null) {
                    sr.setColor(element.color);
                    sr.rect(element.getPixelX(), element.getPixelY(), rectDrawWidth(toIndex - x), pixelSizeModifier);
                }
                x = toIndex;
            }
        }
        sr.end();
    }

    private float rectDrawWidth(int index) {
        return (index * pixelSizeModifier) + pixelSizeModifier;
    }

    public void stepProvidedRows(int minRow, int maxRow) {
        for (int y = minRow; y <= maxRow; y++) {
            Array<Cell> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x).getElement();
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void stepProvidedColumns(int colIndex) {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Cell> row = getRow(y);
            for (int x : shuffledXIndexesForThreads.get(colIndex)) {
                try {
                    Element element = row.get(calculateIndexWithOffset(x)).getElement();
                    if (element != null) {
                        element.step(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int calculateIndexWithOffset(int x) {
        if (x + threadedIndexOffset >= innerArraySize - 1) {
            return (x + threadedIndexOffset) - (innerArraySize - 1);
        } else {
            return x + threadedIndexOffset;
        }
    }

    public void addSpout(ElementType elementType, Vector3 touchPos, int brushSize) {
        spoutArray.add(new Spout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize));
    }

    public void spawnFromSpouts() {
        for (Spout spout : spoutArray) {
            spawnElementByMatrixWithBrush(spout.matrixX, spout.matrixY, spout.sourceElement, spout.brushSize);
        }
    }


    public int toMatrix(float pixelVal) {
        return toMatrix((int) pixelVal);
    }

    public int toMatrix(int pixelVal) {
        return pixelVal / pixelSizeModifier;
    }

    private int toPixel(float matrixVal) {
        return toPixel((int) matrixVal);
    }

    private int toPixel(int matrixVal) {
        return (int) Math.floor(matrixVal * pixelSizeModifier);
    }

    public boolean clearAll() {
        matrix = generateMatrix();
        spoutArray = new Array<>();
        return true;
    }

    public Cell get(float x, float y) {
        return get((int) x, (int) y);
    }

    public Cell get(int x, int y) {
        if (isWithinBounds(x, y)) {
            return matrix.get(y).get(x);
        } else {
            return null;
        }
    }

    public Array<Cell> getRow(int index) {
        return matrix.get(index);
    }

    public boolean setElementAtIndex(int x, int y, Element element) {
        Cell cell = matrix.get(y).get(x);
        cell.setElement(element);
        return true;
    }

    public void spawnElementByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int localBrushSize) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrixWithBrush(matrixX, matrixY, elementType, localBrushSize);
    }

    public void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrix(matrixX, matrixY, elementType);
    }

    public void spawnElementByMatrixWithBrush(int matrixX, int matrixY, ElementType elementType, int localBrushSize) {
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                spawnElementByMatrix(x, y, elementType);
            }
        }
    }

    public void spawnElementByMatrix(int matrixX, int matrixY, ElementType elementType) {
        if (isWithinBounds(matrixX, matrixY) && get(matrixX, matrixY).getClass() != elementType.clazz) {
            setElementAtIndex(matrixX, matrixY, elementType.create(get(matrixX, matrixY)));
        }
    }

    private boolean isWithinBounds(float x, float y) {
        return isWithinBounds((int) x, (int) y);
    }

    public boolean isWithinBounds(int matrixX, int matrixY) {
        return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
    }

    public boolean isWithinXBounds(int matrixX) {
        return matrixX >= 0 && matrixX < innerArraySize;
    }

    public boolean isWithinYBounds(int matrixY) {
        return matrixY >= 0 && matrixY < outerArraySize;
    }

    public List<Integer> getShuffledXIndexes() {
        return shuffledXIndexes;
    }

    public void reshuffleXIndexes() {
        Collections.shuffle(shuffledXIndexes);
    }

    private List<Integer> generateShuffledIndexes(int size) {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    public List<List<Integer>> generateShuffledIndexesForThreads(int threadCount) {
        int colSize = innerArraySize / threadCount;// + (innerArraySize % threadCount);
        List<List<Integer>> indexList = new ArrayList<>(threadCount);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= innerArraySize; i++) {
            if (i % colSize == 0) {
                Collections.shuffle(list);
                indexList.add(list);
                list = new ArrayList<>(colSize);
            }
            list.add(i - 1);
        }
        if (!indexList.contains(list)) {
            indexList.get(indexList.size() - 1).addAll(list);
        }
        shuffledXIndexesForThreads = indexList;
        return indexList;
    }

    public void reshuffleThreadXIndexes(int numThreads) {
        if (shuffledXIndexesForThreads.size() != numThreads) {
            generateShuffledIndexesForThreads(numThreads);
            return;
        }
        shuffledXIndexesForThreads.forEach(Collections::shuffle);
    }

    public void iterateAndSpawnBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize) {

        int matrixX1 = toMatrix((int) pos1.x);
        int matrixY1 = toMatrix((int) pos1.y);
        int matrixX2 = toMatrix((int) pos2.x);
        int matrixY2 = toMatrix((int) pos2.y);

        if (pos1.epsilonEquals(pos2)) {
            spawnElementByMatrixWithBrush(matrixX1, matrixY1, elementType, brushSize);
            return;
        }

        int xDiff = matrixX1 - matrixX2;
        int yDiff = matrixY1 - matrixY2;
        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

        int xModifier = xDiff < 0 ? 1 : -1;
        int yModifier = yDiff < 0 ? 1 : -1;

        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

        int smallerCount = 0;
        for (int i = 1; i <= upperBound; i++) {
            if (freq != 0 && i % freq == 0 && min != smallerCount) {
                smallerCount += 1;
            }
            int yIncrease, xIncrease;
            if (xDiffIsLarger) {
                xIncrease = i;
                yIncrease = smallerCount;
            } else {
                yIncrease = i;
                xIncrease = smallerCount;
            }
            int currentY = matrixY1 + (yIncrease * yModifier);
            int currentX = matrixX1 + (xIncrease * xModifier);
            if (isWithinBounds(currentX, currentY)) {
                spawnElementByMatrixWithBrush(currentX, currentY, elementType, brushSize);
            }
        }


    }

    public Cell getCellByVector(Vector3 lastValidLocation) {
        return get(lastValidLocation.x, lastValidLocation.y);
    }
}
