package com.gdx.cellular.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;

public abstract class MovableSolid extends Solid {

//    public MovableSolid(int x, int y, boolean isPixel) {
//        super(x, y, isPixel);
//    }

    public MovableSolid(Cell cell) {
        super(cell);
    }

//    public void draw(ShapeRenderer sr) {
//        sr.setColor(color);
//        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
//    }

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
        Vector3 lastValidLocation = new Vector3(this.outerCell.matrixLocation.x, this.outerCell.matrixLocation.y, 0);
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

            int modifiedMatrixY = getMatrixY() + (yIncrease * yModifier);
            int modifiedMatrixX = getMatrixX() + (xIncrease * xModifier);
            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
                Cell neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
                if (neighbor == this.outerCell) continue;
                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x = modifiedMatrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                this.outerCell.setElement(null);
            }
        }
    }

    private boolean actOnNeighboringElement(Cell cellNeighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        Element neighbor = cellNeighbor.getElement();
        if (neighbor == null) {
            setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
            if (isFinal) {
                isFreeFalling = true;
//                swapPositions(matrix, neighbor);
                this.outerCell.swapElements(cellNeighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            if (depth > 0) {
                isFreeFalling = true;
                setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
//                swapPositions(matrix, neighbor);
                this.outerCell.swapElements(cellNeighbor);
            } else {
//                moveToLastValidAndSwap(matrix, neighbor, lastValidLocation);
                this.outerCell.moveElementToLastValidAndSwapElements(cellNeighbor, matrix.getCellByVector(lastValidLocation));
                return true;
            }
        } else if (neighbor instanceof Solid) {
            if (depth > 0) return true;
            if (isFinal) {
                this.outerCell.swapElements(matrix.getCellByVector(lastValidLocation));
//                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (isFreeFalling) {
                float absY = Math.max(Math.abs(vel.y) / 31, 105);
                vel.x = vel.x < 0 ? -absY : absY;
            }
            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            Cell diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor * neighbor.frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Cell adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = actOnNeighboringElement(adjacentNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

//            moveToLastValid(matrix, lastValidLocation);
            this.outerCell.swapElements(matrix.getCellByVector(lastValidLocation));
            return true;
        }
        return false;
    }

    private void setAdjacentNeighborsFreeFalling(CellularMatrix matrix, int depth, Vector3 lastValidLocation) {
        if (depth > 0) return;

        Element adjacentNeighbor1 = matrix.get(lastValidLocation.x + 1, lastValidLocation.y).getElement();
        if (adjacentNeighbor1 instanceof Solid) setElementFreeFalling(adjacentNeighbor1);

        Element adjacentNeighbor2 = matrix.get(lastValidLocation.x - 1, lastValidLocation.y).getElement();
        if (adjacentNeighbor2 instanceof Solid) setElementFreeFalling(adjacentNeighbor2);
    }

    private void setElementFreeFalling(Element element) {
        element.isFreeFalling = Math.random() > element.inertialResistance ? true : element.isFreeFalling;
    }



    private int getAdditional(float val) {
        if (val < -.1f) {
            return (int) Math.floor(val);
        } else if (val > .1f) {
            return (int) Math.ceil(val);
        } else {
            return 0;
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
