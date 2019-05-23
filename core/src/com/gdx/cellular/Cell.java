package com.gdx.cellular;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.Element;

import java.util.HashMap;
import java.util.Map;

public class Cell {

    private Element element = null;
    public Vector3 matrixLocation;
    public Vector3 pixelLocation;

    public Cell upLeft = null;
    public Cell up = null;
    public Cell upRight = null;
    public Cell right = null;
    public Cell downRight = null;
    public Cell down = null;
    public Cell downLeft = null;
    public Cell left = null;

    public Map<String, Cell> neighborMap = new HashMap<>();


    public Cell(Vector3 matrixLocation, Vector3 pixelLocation) {
        this.matrixLocation = matrixLocation;
        this.pixelLocation = pixelLocation;
        setUpNeighborMap();
    }

    private void setUpNeighborMap() {
        neighborMap.put("-11", upLeft);
        neighborMap.put("01", up);
        neighborMap.put("11", upRight);
        neighborMap.put("10", right);
        neighborMap.put("1-1", downRight);
        neighborMap.put("0-1", down);
        neighborMap.put("-1-1", downLeft);
        neighborMap.put("-10", left);
    }

    public void setNeighbor(int x, int y, Cell neighbor) {
        String location = String.format("%s%s", x, y);

        switch (location) {
            case "-11":
                upLeft = neighbor;
                break;
            case "01":
                up = neighbor;
                break;
            case "11":
                upRight = neighbor;
                break;
            case "10":
                right = neighbor;
                break;
            case "1-1":
                downRight = neighbor;
                break;
            case "0-1":
                down = neighbor;
                break;
            case "-1-1":
                downLeft = neighbor;
                break;
            case "-10":
                left = neighbor;
                break;
            default:
                throw new RuntimeException("Invalid neighbor location provided while assigning neighbor: " + location);
        }

    }

    public void setElement(Element element) {
        if (element == null) {
            this.element = null;
        } else {
            element.outerCell = this;
            this.element = element;
        }
    }

    public Element getElement() {
        return this.element;
    }

    public void swapElements(Cell otherCell) {
        Element curElement = this.element;
        setElement(otherCell.getElement());
        otherCell.setElement(curElement);
    }

    public void moveElementToLastValidAndSwapElements(Cell toSwap, Cell lastValid) {
        Element curElement = getElement();
        Element toSwapElement = toSwap.getElement();
        Element lastValidElement = lastValid.getElement();

        this.setElement(lastValidElement);
        toSwap.setElement(curElement);
        lastValid.setElement(toSwapElement);
    }
}
