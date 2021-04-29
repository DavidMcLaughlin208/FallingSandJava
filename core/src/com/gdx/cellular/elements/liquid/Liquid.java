package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
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
    public int yDidNotChangeCount = 0;
    public int yDidNotChangeThreshold = 200;

    public Liquid(int x, int y) {
        super(x, y);
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
        float slope = (min == 0 || upperBound == 0) ? 0 : ((float) (min + 1) / (upperBound + 1));

        int smallerCount;
        Vector3 formerLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        for (int i = 1; i <= upperBound; i++) {
            smallerCount = (int) Math.floor(i * slope);

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
                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
                if (neighbor == this) continue;
                boolean stopped = actOnNeighboringElement(neighbor, modifiedMatrixX, modifiedMatrixY, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x = modifiedMatrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                matrix.setElementAtIndex(getMatrixX(), getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(getMatrixX(), getMatrixY()));
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
//        yDidNotChangeCount = yDidNotChange(formerLocation) ? yDidNotChangeCount + 1 : 0;
//        if (yDidNotChangeCount > yDidNotChangeThreshold) {
//            yDidNotChangeCount = yDidNotChangeThreshold;
//        }
//        if (yHasNotMovedBeyondThreshold()) {
//            this.vel.x = 0;
//        }
    }

    private boolean yHasNotMovedBeyondThreshold() {
        return yDidNotChangeCount >= yDidNotChangeThreshold;
    }

    private boolean yDidNotChange(Vector3 formerLocation) {
        return formerLocation.y == this.getMatrixY();
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
                if (isFinal) {
                    swapLiquidForDensities(matrix, liquidNeighbor, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                    return true;
                } else {
                    lastValidLocation.x = modifiedMatrixX;
                    lastValidLocation.y = modifiedMatrixY;
                    return false;
                }
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
//            if (yHasNotMovedBeyondThreshold()) {
//                vel.x = Math.random() > 0.5 ? 64 : -64;
//            }
            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY() + additionalY, distance, lastValidLocation);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance, lastValidLocation);
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

            Element diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = -124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY() + additionalY, distance, lastValidLocation);
                if (!stoppedDiagonally) {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance, lastValidLocation);
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

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance, Vector3 lastValid) {
        int distanceModifier = distance > 0 ? 1 : -1;
        Vector3 lastValidLocation = lastValid.cpy();
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
                lastValidLocation.x = modifiedX;
                lastValidLocation.y = startingY;
            } else if (neighbor instanceof Liquid) {
                Liquid liquidNeighbor = (Liquid) neighbor;
                if (isFinal) {
                    if (compareDensities(liquidNeighbor)) {
                        swapLiquidForDensities(matrix, liquidNeighbor, modifiedX, startingY, lastValidLocation);
                        return false;
                    }
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

    private void swapLiquidForDensities(CellularMatrix matrix, Liquid neighbor, int neighborX, int neighborY, Vector3 lastValidLocation) {
        vel.y = -62;
        if (Math.random() > 0.8f) {
            vel.x *= -1;
        }
        moveToLastValidAndSwap(matrix, neighbor, neighborX, neighborY, lastValidLocation);
    }

    private boolean compareDensities(Liquid neighbor) {
        return (density > neighbor.density && neighbor.getMatrixY() <= getMatrixY()); // ||  (density < neighbor.density && neighbor.matrixY >= matrixY);
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
    public void darkenColor() { }

    @Override
    public void darkenColor(float factor) { }

    @Override
    public boolean stain(float r, float g, float b, float a) {
        return false;
    }

    @Override
    public boolean stain(Color color) {
        return  false;
    }
}
