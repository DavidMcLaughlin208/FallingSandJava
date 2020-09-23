package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.particles.Particle;

public abstract class MovableSolid extends Solid {

    public MovableSolid(int x, int y) {
        super(x, y);
        stoppedMovingThreshold = 5;
    }

    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        if (this.owningBody != null) {
            stepAsPartOfPhysicsBody(matrix);
            return;
        }
        if (matrix.useChunks && !matrix.shouldElementInChunkStep(this)) {
            return;
        }
        vel.add(CellularAutomaton.gravity);
        if (isFreeFalling) vel.x *= .9;

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        float velYDeltaTimeFloat = (Math.abs(vel.y) * 1/60);
        float velXDeltaTimeFloat = (Math.abs(vel.x) * 1/60);
        int velXDeltaTime;
        int velYDeltaTime;
        if (velXDeltaTimeFloat < 1) {
            xThreshold += velXDeltaTimeFloat;
            velXDeltaTime = (int) xThreshold;
            if (Math.abs(velXDeltaTime) > 0) {
                xThreshold = 0;
            }
        } else {
            xThreshold = 0;
            velXDeltaTime = (int) velXDeltaTimeFloat;
        }
        if (velYDeltaTimeFloat < 1) {
            yThreshold += velYDeltaTimeFloat;
            velYDeltaTime = (int) yThreshold;
            if (Math.abs(velYDeltaTime) > 0) {
                yThreshold = 0;
            }
        } else {
            yThreshold = 0;
            velYDeltaTime = (int) velYDeltaTimeFloat;
        }

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        float floatFreq = (min == 0 || upperBound == 0) ? 0 : ((float) min / upperBound);
        int freqThreshold = 0;
        float freqCounter = 0;

        int smallerCount = 0;
        Vector3 formerLocation = new Vector3(matrixX, matrixY, 0);
        Vector3 lastValidLocation = new Vector3(matrixX, matrixY, 0);
        for (int i = 1; i <= upperBound; i++) {
            freqCounter += floatFreq;
            boolean thresholdPassed = Math.floor(freqCounter) > freqThreshold;
            if (floatFreq != 0 && thresholdPassed && min >= smallerCount) {
                freqThreshold = (int) Math.floor(freqCounter);
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
                boolean stopped = actOnNeighboringElement(neighbor, modifiedMatrixX, modifiedMatrixY, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x = modifiedMatrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTYCELL.createElementByMatrix(matrixX, matrixY));
                return;
            }
        }
        applyHeatToNeighborsIfIgnited(matrix);
        takeEffectsDamage(matrix);
        spawnSparkIfIgnited(matrix);
        checkLifeSpan(matrix);
        modifyColor();
        stoppedMovingCount = didNotMove(formerLocation) && !isIgnited ? stoppedMovingCount + 1 : 0;
        if (stoppedMovingCount > stoppedMovingThreshold) {
            stoppedMovingCount = stoppedMovingThreshold;
        }
        if (matrix.useChunks) {
            if (isFreeFalling || isIgnited || !hasNotMovedBeyondThreshold()) {
                matrix.reportToChunkActive(this);
                matrix.reportToChunkActive((int) formerLocation.x, (int) formerLocation.y);
            }
        }
    }

    private void stepAsPartOfPhysicsBody(CellularMatrix matrix) {
        applyHeatToNeighborsIfIgnited(matrix);
        takeEffectsDamage(matrix);
        spawnSparkIfIgnited(matrix);
        checkLifeSpan(matrix);
        modifyColor();
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
            if (isFinal) {
                isFreeFalling = true;
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            if (depth > 0) {
                isFreeFalling = true;
                setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
            } else {
                isFreeFalling = true;
                moveToLastValidAndSwap(matrix, neighbor, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                return true;
            }
        } else if (neighbor instanceof Solid) {
            if (depth > 0) return true;
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
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor * neighbor.frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = actOnNeighboringElement(diagonalNeighbor, matrixX + additionalX, matrixY + additionalY, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null  && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = actOnNeighboringElement(adjacentNeighbor, matrixX + additionalX, matrixY, matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

            moveToLastValid(matrix, lastValidLocation);
            return true;
        } else if (neighbor instanceof Gas) {
            if (isFinal) {
                moveToLastValidAndSwap(matrix, neighbor, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                return true;
            }
            return false;
        }
        return false;
    }

    private void setAdjacentNeighborsFreeFalling(CellularMatrix matrix, int depth, Vector3 lastValidLocation) {
        if (depth > 0) return;

        Element adjacentNeighbor1 = matrix.get(lastValidLocation.x + 1, lastValidLocation.y);
        if (adjacentNeighbor1 instanceof Solid) {
            boolean wasSet = setElementFreeFalling(adjacentNeighbor1);
            if (wasSet) {
                matrix.reportToChunkActive(adjacentNeighbor1);
            }
        }

        Element adjacentNeighbor2 = matrix.get(lastValidLocation.x - 1, lastValidLocation.y);
        if (adjacentNeighbor2 instanceof Solid) {
            boolean wasSet = setElementFreeFalling(adjacentNeighbor2);
            if (wasSet) {
                matrix.reportToChunkActive(adjacentNeighbor2);
            }
        }
    }

    private boolean setElementFreeFalling(Element element) {
        element.isFreeFalling = Math.random() > element.inertialResistance || element.isFreeFalling;
        return element.isFreeFalling;
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
