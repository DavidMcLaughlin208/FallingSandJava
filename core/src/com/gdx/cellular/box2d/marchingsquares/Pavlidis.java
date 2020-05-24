package com.gdx.cellular.box2d.marchingsquares;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;

import java.util.ArrayList;
import java.util.List;

public class Pavlidis {


    public static List<Vector2> getOutliningVerts(Array<Array<Element>> elements) {
        List<Vector2> outliningVerts = new ArrayList<>();
        // Get starting point
        Element startingPoint = null;
        Vector2 startingVector = null;
        for (int y = elements.size - 1; y >= 0; y--) {
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
        Element currentElement = null;
        Vector2 currentLocation = startingVector.cpy();
        Direction currentDirection = Direction.North;
        Vector2 upLeftVector, upVector, upRightVector;
        Element upLeft, up, upRight;
        while (currentElement != startingPoint) {
            switch (currentDirection) {
                case North:
                    upLeftVector = getDirectionalVector(currentLocation, Direction.North, DirectionalVector.UpLeft);
                    upLeft = getFromArray(upLeftVector, elements);
                    if (upLeft != null) {
                        currentElement = upLeft;
                        currentLocation.set(upLeftVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.West;
                        continue;
                    }
                    upVector = getDirectionalVector(currentLocation, Direction.North, DirectionalVector.Up);
                    up = getFromArray(upVector, elements);
                    if (up != null) {
                        currentElement = up;
                        currentLocation.set(upVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.North;
                        continue;
                    }
                    upRightVector = getDirectionalVector(currentLocation, Direction.North, DirectionalVector.UpRight);
                    upRight = getFromArray(upRightVector, elements);
                    if (upRight != null) {
                        currentElement = upRight;
                        currentLocation.set(upRightVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.East;
                        continue;
                    }
                case East:
                    upLeftVector = getDirectionalVector(currentLocation, Direction.East, DirectionalVector.UpLeft);
                    upLeft = getFromArray(upLeftVector, elements);
                    if (upLeft != null) {
                        currentElement = upLeft;
                        currentLocation.set(upLeftVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.North;
                        continue;
                    }
                    upVector = getDirectionalVector(currentLocation, Direction.East, DirectionalVector.Up);
                    up = getFromArray(upVector, elements);
                    if (up != null) {
                        currentElement = up;
                        currentLocation.set(upVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.East;
                        continue;
                    }
                    upRightVector = getDirectionalVector(currentLocation, Direction.East, DirectionalVector.UpRight);
                    upRight = getFromArray(upRightVector, elements);
                    if (upRight != null) {
                        currentElement = upRight;
                        currentLocation.set(upRightVector);
                        outliningVerts.add(currentLocation.cpy());
                        currentDirection = Direction.South;
                        continue;
                    }
            }
        }
        return outliningVerts;
    }

    private static Vector2 getDirectionalVector(Vector2 current, Direction direction, DirectionalVector directionalVector) {
        switch (direction) {
            case North:
                switch (directionalVector) {
                    case UpLeft:
                        return new Vector2(current.x - 1, current.y - 1);
                    case Up:
                        return new Vector2(current.x, current.y - 1);
                    case UpRight:
                        return new Vector2(current.x + 1, current.y - 1);
                }
            case East:
                switch (directionalVector) {
                    case UpLeft:
                        return new Vector2(current.x + 1,  current.y - 1);
                    case Up:
                        return new Vector2(current.x + 1,  current.y);
                    case UpRight:
                        return new Vector2(current.x + 1,  current.y + 1);
                }
            case South:
                switch (directionalVector) {
                    case UpLeft:
                        return new Vector2(current.x + 1, current.y + 1);
                    case Up:
                        return new Vector2(current.x, current.y + 1);
                    case UpRight:
                        return new Vector2(current.x - 1, current.y + 1);
                }
            case West:
                switch (directionalVector) {
                    case UpLeft:
                        return new Vector2(current.x - 1, current.y + 1);
                    case Up:
                        return new Vector2(current.x - 1, current.y);
                    case UpRight:
                        return new Vector2(current.x - 1, current.y - 1);
                }
            default:
                throw new IllegalStateException("Impossible combination of Direction and DirectionalVector");
        }
    }

    private static Element getFromArray(Vector2 cur, Array<Array<Element>> elements) {
        if (cur.y > elements.size || cur.y < 0 || cur.x > elements.get(0).size - 1 || cur.x < 0) {
            return null;
        }
        return elements.get((int) cur.y).get((int) cur.x);
    }

    private enum Direction {
        North,
        East,
        South,
        West
    }

    private enum DirectionalVector {
        UpLeft,
        Up,
        UpRight
    }
}
