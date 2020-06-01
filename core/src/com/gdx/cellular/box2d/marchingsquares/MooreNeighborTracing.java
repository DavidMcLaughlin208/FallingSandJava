package com.gdx.cellular.box2d.marchingsquares;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MooreNeighborTracing {

    private MooreNeighborTracing() { throw new IllegalStateException("Cannot instantiate MooreNeighborTracing"); }

    private static final Map<Integer, Vector2> neighborRelativeLocationMap = new HashMap<>();
    private static final Map<IncomingDirection, Integer> directionIndexOffsetMap = new HashMap<>();
    private static final Map<Integer, IncomingDirection> newRelativeDirectionMap = new HashMap<>();

    static {
        neighborRelativeLocationMap.put(1, new Vector2(0, -1));
        neighborRelativeLocationMap.put(2, new Vector2(1, -1));
        neighborRelativeLocationMap.put(3, new Vector2(1, 0));
        neighborRelativeLocationMap.put(4, new Vector2(1, 1));
        neighborRelativeLocationMap.put(5, new Vector2(0, 1));
        neighborRelativeLocationMap.put(6, new Vector2(-1, 1));
        neighborRelativeLocationMap.put(7, new Vector2(-1, 0));
        neighborRelativeLocationMap.put(8, new Vector2(-1, -1));

        directionIndexOffsetMap.put(IncomingDirection.North, 0);
        directionIndexOffsetMap.put(IncomingDirection.West, 2);
        directionIndexOffsetMap.put(IncomingDirection.South, 4);
        directionIndexOffsetMap.put(IncomingDirection.East, 6);

        newRelativeDirectionMap.put(1, IncomingDirection.East);
        newRelativeDirectionMap.put(2, IncomingDirection.East);
        newRelativeDirectionMap.put(3, IncomingDirection.North);
        newRelativeDirectionMap.put(4, IncomingDirection.North);
        newRelativeDirectionMap.put(5, IncomingDirection.West);
        newRelativeDirectionMap.put(6, IncomingDirection.West);
        newRelativeDirectionMap.put(7, IncomingDirection.South);
        newRelativeDirectionMap.put(8, IncomingDirection.South);

    }

    public static List<Vector2> getOutliningVerts(Array<Array<Element>> elements) {
        List<Vector2> outliningVerts = new ArrayList<>();
        Element startingPoint = null;
        Vector2 startingVector = null;
        // Brute force from the bottom left of the matrix to find starting element
        for (int y = elements.size - 1; y >= 0; y--) {
            if (startingVector != null) break;
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    startingPoint = element;
                    startingVector = new Vector2(x, y);
                    outliningVerts.add(startingVector.cpy());
                    break;
                }
            }
        }
        if (startingPoint == null) {
            return outliningVerts;
        }
//        List<Element> elementList = new ArrayList<>();
        Element currentElement;
        Vector2 currentLocation = startingVector.cpy();
        IncomingDirection currentIncomingDirection = IncomingDirection.East;
        boolean endConditionMet = false;
        IncomingDirection incomingDirectionToFinalPoint = null;
        int neighborsVisited = 0;
        while (!endConditionMet) {
            if (neighborsVisited >= 10000) {
                System.out.println("Stuck in infinite loop");
                return outliningVerts;
            }
            neighborsVisited++;
            for (int i = 1; i <= 8; i++) {
                Vector2 neighborLocation = getNeighboringElementLocationForIndexAndDirection(i, currentIncomingDirection, currentLocation, elements);
                Element neighbor = getNeighboringElement(neighborLocation, elements);
                if (neighbor == null) {
                    if (i == 8) {
                        // Element is an island
                        endConditionMet = true;
                        break;
                    }
                    continue;
                }
                currentElement = neighbor;
                currentLocation = neighborLocation.cpy();
                currentIncomingDirection = getNewIncomingDirection(i, currentIncomingDirection);
                if (currentElement == startingPoint) {
                    endConditionMet = true;
                    break;
                }
                int indexOfCurrentLocation = outliningVerts.indexOf(currentLocation);
                if (indexOfCurrentLocation == -1) {
                    outliningVerts.add(neighborLocation.cpy());
//                    elementList.add(currentElement);
                } else {
                    outliningVerts.remove(indexOfCurrentLocation);
                }
                break;
            }
        }
        return outliningVerts;
    }

    private static IncomingDirection getNewIncomingDirection(int i, IncomingDirection currentIncomingDirection) {
        int offsetIndex = getOffsetIndex(i, currentIncomingDirection);
        return newRelativeDirectionMap.get(offsetIndex);
    }

    private static Vector2 getNeighboringElementLocationForIndexAndDirection(int i, IncomingDirection currentIncomingDirection, Vector2 currentLocation, Array<Array<Element>> elements) {
        int offsetIndex = getOffsetIndex(i, currentIncomingDirection);
        return getNeighborLocation(offsetIndex, currentLocation);
    }

    private static Element getNeighboringElement(Vector2 neighborLocation, Array<Array<Element>> elements) {
        if (neighborLocation.y >= elements.size || neighborLocation.y < 0 || neighborLocation.x >= elements.get((int) neighborLocation.y).size || neighborLocation.x < 0) {
            return null;
        } else {
            return elements.get((int) neighborLocation.y).get((int) neighborLocation.x);
        }
    }

    private static Vector2 getNeighborLocation(int offsetIndex, Vector2 currentLocation) {
        Vector2 offsetVector = neighborRelativeLocationMap.get(offsetIndex);
        return new Vector2(currentLocation.x + offsetVector.x, currentLocation.y + offsetVector.y);
    }

    private static int getOffsetIndex(int i, IncomingDirection currentIncomingDirection) {
        int newIndex = i + directionIndexOffsetMap.get(currentIncomingDirection);
        if (newIndex > 8) {
            return newIndex - 8;
        } else {
            return newIndex;
        }
    }

    private enum IncomingDirection {
        North,
        South,
        East,
        West
    }

}
