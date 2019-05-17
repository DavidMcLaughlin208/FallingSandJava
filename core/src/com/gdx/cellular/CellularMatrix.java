package com.gdx.cellular;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

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

    private Array<Array<Element>> matrix;

    public CellularMatrix(int width, int height, int pixelSizeModifier) {
        this.pixelSizeModifier = pixelSizeModifier;
        innerArraySize = toMatrix(width);
        outerArraySize = toMatrix(height);
        matrix = generateMatrix();
        shuffledXIndexes = generateShuffledIndexes(innerArraySize);
        calculateAndSetThreadedXIndexOffset();
    }

    public void calculateAndSetThreadedXIndexOffset() {
        if (shuffledXIndexesForThreads != null) {
            threadedIndexOffset = (int) (Math.random() * (innerArraySize / shuffledXIndexesForThreads.size()));
        } else {
            threadedIndexOffset = 0;
        }
    }

    private Array<Array<Element>> generateMatrix() {
        Array<Array<Element>> outerArray = new Array<>(true, outerArraySize);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> innerArr = new Array<>(true, innerArraySize);
            for (int x = 0; x < innerArraySize; x++) {
                innerArr.add(ElementType.EMPTY_CELL.createElementByMatrix(x, y));
            }
            outerArray.add(innerArr);
        }
        return outerArray;
    }

    public void stepAndDrawAll(ShapeRenderer sr) {
        stepAll();
        drawAll(sr);
    }

    private void stepAll() {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
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
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    if (get(following, y).color != currentColor) {
                        break;
                    }
                    toIndex = following;
                }
                x = toIndex;
                if (element != null) {
                    sr.setColor(element.color);
                    sr.rect(element.pixelX, element.pixelY, rectDrawWidth(toIndex), pixelSizeModifier);
                }
            }
        }
        sr.end();
    }

    private float rectDrawWidth(int index) {
        return (index * pixelSizeModifier) + (pixelSizeModifier - 1);
    }

    public void stepProvidedRows(int minRow, int maxRow) {
        for (int y = minRow; y <= maxRow; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void stepProvidedColumns(int colIndex) {

        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : shuffledXIndexesForThreads.get(colIndex)) {
                try {
                    Element element = row.get(calculateIndexWithOffset(x));
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
        if (x + threadedIndexOffset > innerArraySize - 1) {
            return (x + threadedIndexOffset) - (innerArraySize - 1);
        } else {
            return x + threadedIndexOffset;
        }
    }

    public int toMatrix(float pixelVal) {
        return toMatrix((int) pixelVal);
    }

    public int toMatrix(int pixelVal) {
        return pixelVal / pixelSizeModifier;
    }

    public boolean clearAll() {
        matrix = generateMatrix();
        return true;
    }

    public Element get(float x, float y) {
        return get((int) x, (int) y);
    }

    public Element get(int x, int y) {
        if (isWithinBounds(x, y)) {
            return matrix.get(y).get(x);
        } else {
            return null;
        }
    }

    public Array<Element> getRow(int index) {
        return matrix.get(index);
    }

    public boolean setElementAtIndex(int x, int y, Element element) {
        matrix.get(y).set(x, element);
        element.setCoordinatesByMatrix(x, y);
        return true;
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
}
