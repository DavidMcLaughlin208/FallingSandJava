package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.particles.Particle;

public abstract class Gas extends Element {

    public int density;
    public int dispersionRate;

    public Gas(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
    }

    @Override
    public void spawnSparkIfIgnited(CellularMatrix matrix) {}

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.sub(CellularAutomaton.gravity);
        vel.y = Math.min(vel.y, 124);
        if (vel.y == 124 && Math.random() > .5) {
            vel.y = 64;
        }
        vel.x *= .9;
//        if (vel.x == 0 && Math.random() > .8) {
//            vel.x = 64;
//        }

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
            if (freq != 0 && i % freq == 0 && min >= smallerCount) {
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
                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTYCELL.createElementByPixel(pixelX, pixelY));
                return;
            }
        }
        applyHeatToNeighborsIfIgnited(matrix);
        modifyColor();
        spawnSparkIfIgnited(matrix);
        checkLifeSpan(matrix);
        takeEffectsDamage(matrix);
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        boolean acted = actOnOther(neighbor, matrix);
        if (acted) return true;
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Gas) {
            Gas gasNeighbor = (Gas) neighbor;
            if (compareGasDensities(gasNeighbor)) {
                swapGasForDensities(matrix, gasNeighbor, lastValidLocation);
                return false;
            }
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }

            vel.x = vel.x < 0 ? -62 : 62;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, diagonalNeighbor.matrixX, diagonalNeighbor.matrixY, distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, adjacentNeighbor.matrixX, adjacentNeighbor.matrixY, distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        } else if (neighbor instanceof Liquid) {
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (neighbor.isFreeFalling) {
                return true;
            }
            float absY = Math.max(Math.abs(vel.y) / 31, 105);
            vel.x = vel.x < 0 ? -absY : absY;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, diagonalNeighbor.matrixX, diagonalNeighbor.matrixY, distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, adjacentNeighbor.matrixX, adjacentNeighbor.matrixY, distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

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
            if (neighbor.isFreeFalling) {
                return true;
            }

            float absY = Math.max(Math.abs(vel.y) / 31, 105);
            vel.x = vel.x < 0 ? -absY : absY;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, diagonalNeighbor.matrixX, diagonalNeighbor.matrixY, distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(matrixX + additionalX, matrixY);
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, adjacentNeighbor.matrixX, adjacentNeighbor.matrixY, distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        return false;
    }

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance) {
        int distanceModifier = distance > 0 ? 1 : -1;
        Vector3 lastValidLocation = new Vector3(matrixX, matrixY, 0);
        for (int i = 0; i <= Math.abs(distance); i++) {
            Element neighbor = matrix.get(startingX + i * distanceModifier, startingY);
            boolean acted = actOnOther(neighbor, matrix);
            if (acted) return false;
            boolean isFirst = i == 0;
            boolean isFinal = i == Math.abs(distance);
            if (neighbor == null) continue;
            if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
                if (isFinal) {
                    swapPositions(matrix, neighbor);
                    return false;
                }
                lastValidLocation.x = startingX + i * distanceModifier;
                lastValidLocation.y = startingY;
                continue;
            } else if (neighbor instanceof Gas) {
                Gas gasNeighbor = (Gas) neighbor;
                if (compareGasDensities(gasNeighbor)) {
                    swapGasForDensities(matrix, gasNeighbor, lastValidLocation);
                    return false;
                }
            } else if (neighbor instanceof Solid || neighbor instanceof Liquid) {
                if (isFirst) {
                    return true;
                }
                moveToLastValid(matrix, lastValidLocation);
                return false;
            }
        }
        return true;
    }

    private void swapGasForDensities(CellularMatrix matrix, Gas neighbor, Vector3 lastValidLocation) {
        vel.y = 62;
        moveToLastValidAndSwap(matrix, neighbor, lastValidLocation);
    }

    private boolean compareGasDensities(Gas neighbor) {
        return (density > neighbor.density && neighbor.matrixY <= matrixY); // ||  (density < neighbor.density && neighbor.matrixY >= matrixY);
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
        if (otherVel > 125f) {
            return 124f;
        }
        float avg = (vel + otherVel) / 2;
        if (avg > 0) {
            return avg;
        } else {
            return Math.min(avg, 124f);
        }
    }
}
