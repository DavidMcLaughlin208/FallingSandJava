package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.particles.Particle;

public abstract class Liquid extends Element {

    public int density;
    public int dispersionRate;

    public Liquid(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        stoppedMovingThreshold = 10;
    }

    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);

        if (matrix.useChunks && !matrix.shouldElementInChunkStep(this)) {
            return;
        }

        vel.add(CellularAutomaton.gravity);
        if (isFreeFalling) vel.x *= .8;

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        int velYDeltaTime = (int) (Math.abs(vel.y) * Gdx.graphics.getDeltaTime());
        int velXDeltaTime = (int) (Math.abs(vel.x) * Gdx.graphics.getDeltaTime());

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
        modifyColor();
        spawnSparkIfIgnited(matrix);
        checkLifeSpan(matrix);
        takeEffectsDamage(matrix);
        stoppedMovingCount = didNotMove(formerLocation) ? stoppedMovingCount + 1 : 0;
        if (stoppedMovingCount > stoppedMovingThreshold) {
            stoppedMovingCount = stoppedMovingThreshold;
        }
        if (matrix.useChunks)  {
            if (isIgnited || !hasNotMovedBeyondThreshold()) {
                matrix.reportToChunkActive(this);
                matrix.reportToChunkActive((int) formerLocation.x, (int) formerLocation.y);
            }
        }
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        boolean acted = actOnOther(neighbor, matrix);
        if (acted) return true;
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
            if (isFinal) {
                isFreeFalling = true;
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            Liquid liquidNeighbor = (Liquid) neighbor;
            if (compareDensities(liquidNeighbor)) {
                swapLiquidForDensities(matrix, liquidNeighbor, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                return false;
            }
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

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, matrixX + additionalX, matrixY + additionalY, distance);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, matrixX + additionalX, matrixY, distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

            moveToLastValid(matrix, lastValidLocation);
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

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, matrixX + additionalX, matrixY + additionalY, distance);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, matrixX + additionalX, matrixY, distance);
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

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance) {
        int distanceModifier = distance > 0 ? 1 : -1;
        Vector3 lastValidLocation = new Vector3(matrixX, matrixY, 0);
        for (int i = 0; i <= Math.abs(distance); i++) {
            int modifiedX = startingX + i * distanceModifier;
            Element neighbor = matrix.get(modifiedX, startingY);
            if (neighbor == null) {
                return true;
            }
            boolean acted = actOnOther(neighbor, matrix);
            if (acted) return false;
            boolean isFirst = i == 0;
            boolean isFinal = i == Math.abs(distance);
            if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
                if (isFinal) {
                    swapPositions(matrix, neighbor, modifiedX, startingY);
                    return false;
                }
                lastValidLocation.x = startingX + i * distanceModifier;
                lastValidLocation.y = startingY;
            } else if (neighbor instanceof Liquid) {
                Liquid liquidNeighbor = (Liquid) neighbor;
                if (compareDensities(liquidNeighbor)) {
                    swapLiquidForDensities(matrix, liquidNeighbor, modifiedX, startingY, lastValidLocation);
                    return false;
                }
            } else if (neighbor instanceof Solid) {
                if (isFirst) {
                    return true;
                }
                moveToLastValid(matrix, lastValidLocation);
                return false;
            }
        }
        return true;
    }

    private void swapLiquidForDensities(CellularMatrix matrix, Liquid neighbor, int neighorX, int neighborY, Vector3 lastValidLocation) {
        vel.y = -62;
        moveToLastValidAndSwap(matrix, neighbor, neighorX, neighborY, lastValidLocation);
    }

    private boolean compareDensities(Liquid neighbor) {
        return (density > neighbor.density && neighbor.matrixY <= matrixY); // ||  (density < neighbor.density && neighbor.matrixY >= matrixY);
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

    @Override
    public boolean infect(CellularMatrix matrix) {
        return false;
    }

    @Override
    public void darkenColor() {
        return;
    }

    @Override
    public void darkenColor(float factor) {
        return;
    }

}
