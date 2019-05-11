package com.gdx.cellular.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;

public class Sand extends Element implements Solid {

    private boolean isFreeFalling = true;

    public Sand(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(Math.random() > 0.5 ? -1 : 1, -124f,0f);
        frictionFactor = 0.9f;
    }

    public Color color = Color.YELLOW;

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.add(CellularAutomaton.gravity);

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        int velYDeltaTime = (int) (Math.abs(vel.y) * Gdx.graphics.getDeltaTime());
        int velXDeltaTime = (int) (Math.abs(vel.x) * Gdx.graphics.getDeltaTime());

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

        int smallerCount = 0;
        Vector3 lastValidLocation = new Vector3(matrixX, matrixY, 0);
        for (int i = 1; i <= upperBound; i++) {
            if (freq != 0 && i % freq == 0 && min <= smallerCount) {
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

            int modifiedMatrixY = matrixY + (yIncrease * yModifier);
            int modifiedMatrixX = matrixX + (xIncrease * xModifier);
            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
                if (neighbor == this) continue;
                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x = modifiedMatrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTY_CELL.createElementByPixel(pixelX, pixelY));
            }
        }
    }

    private boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell) {
            if (isFinal) {
                isFreeFalling = true;
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            swapPositions(matrix, neighbor);
            return true;
        } else if (neighbor instanceof Solid) {
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (isFreeFalling) {
                float absY = Math.max(Math.abs(vel.y) / 31, 105);
                vel.x = vel.x < 0 ? -absY : absY;
            }
            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
//            vel.x += additionalX;
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
//            vel.x = (vel.x + neighbor.vel.x) / 2 * frictionFactor;
            vel.x *= frictionFactor * neighbor.frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = actOnNeighboringElement(adjacentNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        return false;
    }

//    private boolean iterateBetweenTwoPointsAndAct(int matrixX, int matrixY, int xFactor, int yFactor, CellularMatrix matrix) {
//        int yModifier = yFactor < 0 ? -1 : 1;
//        int xModifier = yFactor < 0 ? -1 : 1;
//
//        boolean xDiffIsLarger = Math.abs(xFactor) > Math.abs(yFactor);
//
//        int upperBound = Math.max(Math.abs(xFactor), Math.abs(yFactor));
//        int min = Math.min(Math.abs(xFactor), Math.abs(yFactor));
//        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);
//
//        int smallerCount = 0;
//        Vector2 lastValidLocation = new Vector2(matrixX, matrixY);
//        for (int i = 1; i <= upperBound; i++) {
//            if (freq != 0 && i % freq == 0 && min <= smallerCount) {
//                smallerCount += 1;
//            }
//
//            int yIncrease, xIncrease;
//            if (xDiffIsLarger) {
//                xIncrease = i;
//                yIncrease = smallerCount;
//            } else {
//                yIncrease = i;
//                xIncrease = smallerCount;
//            }
//
//            int modifiedMatrixY = matrixY + (yIncrease * yModifier);
//            int modifiedMatrixX = matrixX + (xIncrease * xModifier);
//            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
//                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
//                if (neighbor == this) continue;
//                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == upperBound, false, lastValidLocation, 0);
//                if (stopped) {
//                    return stopped;
//                }
//                lastValidLocation.x =  matrixX;
//                lastValidLocation.y = modifiedMatrixY;
//
//            } else {
//                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTY_CELL.createElementByPixel(pixelX, pixelY));
//                return true;
//            }
//        }
//        return true;
//    }

    private int getAdditional(float val) {
        if (val < -.1f) {
            return (int) Math.floor(val);
        } else if (val > .1f) {
            return (int) Math.ceil(val);
        } else {
            return 0; //Math.random() > 0.5 ? 1 : -1;
        }
    }

    private float getAverageVelOrGravity(float vel, float otherVel) {
        if (otherVel > -125f) {
            return -124f;
        }
        float avg = (vel + otherVel) / 2;
        if (avg > 0) {
            return avg;
        } else {
            return Math.min(avg, -124f);
        }
    }
}
