package com.gdx.cellular;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.NeighborLocation;

import java.util.HashMap;
import java.util.Map;

public class Cell {

    private Element element = null;
    public Vector3 matrixLocation;
    public Vector3 pixelLocation;

    public Map<NeighborLocation, Cell> neighborMap = new HashMap<>();


    public Cell(Vector3 matrixLocation, Vector3 pixelLocation) {
        this.matrixLocation = matrixLocation;
        this.pixelLocation = pixelLocation;
    }

    public Cell getNeighbor(NeighborLocation location) {
        return neighborMap.get(location);
    }

    public void setNeighbor(NeighborLocation location, Cell neighbor) {
        switch (location) {
            case UPLEFT:
                neighborMap.put(NeighborLocation.UPLEFT, neighbor);
                break;
            case UP:
                neighborMap.put(NeighborLocation.UP, neighbor);
                break;
            case UPRIGHT:
                neighborMap.put(NeighborLocation.UPRIGHT, neighbor);
                break;
            case RIGHT:
                neighborMap.put(NeighborLocation.RIGHT, neighbor);
                break;
            case DOWNRIGHT:
                neighborMap.put(NeighborLocation.DOWNRIGHT, neighbor);
                break;
            case DOWN:
                neighborMap.put(NeighborLocation.DOWN, neighbor);
                break;
            case DOWNLEFT:
                neighborMap.put(NeighborLocation.DOWNLEFT, neighbor);
                break;
            case LEFT:
                neighborMap.put(NeighborLocation.LEFT, neighbor);
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

        if (this == lastValid) {
            swapElements(toSwap);
            return;
        }
        Element curElement = getElement();
        Element toSwapElement = toSwap.getElement();
        Element lastValidElement = lastValid.getElement();

        this.setElement(lastValidElement);
        toSwap.setElement(curElement);
        lastValid.setElement(toSwapElement);
    }
}
