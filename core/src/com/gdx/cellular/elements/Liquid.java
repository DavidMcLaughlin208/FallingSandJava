package com.gdx.cellular.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;

public abstract class Liquid extends Element {

    public int density;
    public int dispersionRate;
    public boolean swappedDensityThisStep = false;

    public Liquid(Cell cell) {
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
        swappedDensityThisStep = false;

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        int velYDeltaTime = (int) (Math.abs(vel.y) * Gdx.graphics.getDeltaTime());
        int velXDeltaTime = (int) (Math.abs(vel.x) * Gdx.graphics.getDeltaTime());

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

        int smallerCount = 0;
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
//        if (this.outerCell.up != null && this.outerCell.up.getElement() instanceof Liquid &&
//                density < ((Liquid) this.outerCell.up.getElement()).density && this.getClass() != this.outerCell.up.getElement().getClass() && !((Liquid) this.outerCell.up.getElement()).swappedDensityThisStep) {
//            this.swappedDensityThisStep = true;
//            this.outerCell.swapElements(this.outerCell.up);
//            return;
//        }
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
                break;
            }
        }
    }

    private boolean actOnNeighboringElement(Cell cellNeighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        Element neighbor = cellNeighbor.getElement();
        if (neighbor == null) {
            setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
            if (isFinal) {
                isFreeFalling = true;
                this.outerCell.swapElements(cellNeighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            isFreeFalling = true;
            if (density >= ((Liquid) neighbor).density && this.getClass() != neighbor.getClass() && neighbor.getMatrixY() <= getMatrixY() && !((Liquid) neighbor).swappedDensityThisStep) {
                swappedDensityThisStep = true;
                this.outerCell.moveElementToLastValidAndSwapElements(cellNeighbor, matrix.getCellByVector(lastValidLocation));
                return true;
            }
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                this.outerCell.swapElements(matrix.getCellByVector(lastValidLocation));
                return true;
            }
            if (isFreeFalling) {
                float absY = Math.max(Math.abs(vel.y) / 31, 105);
                vel.x = vel.x < 0 ? -absY : absY;
            }
            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            additionalX *= Math.random() > 0.5 ? dispersionRate + 1 : dispersionRate - 1;

            Cell diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Cell adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = actOnNeighboringElement(adjacentNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

            this.outerCell.swapElements(matrix.getCellByVector(lastValidLocation));
            return true;
        } else if (neighbor instanceof Solid) {
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                this.outerCell.swapElements(matrix.getCellByVector(lastValidLocation));
                return true;
            }
            if (isFreeFalling) {
                float absY = Math.max(Math.abs(vel.y) / 31, 105);
                vel.x = vel.x < 0 ? -absY : absY;
            }
            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            additionalX *= Math.random() > 0.5 ? dispersionRate + 1 : dispersionRate - 1;

            Cell diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Cell adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = actOnNeighboringElement(adjacentNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

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
        element.isFreeFalling = Math.random() > element.inertialResistance || element.isFreeFalling;
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
