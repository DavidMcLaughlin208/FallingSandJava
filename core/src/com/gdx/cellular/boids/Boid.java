package com.gdx.cellular.boids;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.Solid;

public class Boid extends Element {

    public static int neighborDistance = 15;
    private static final int noiseFactor = 55;

    public Boid(int x, int y, Vector3 velocity) {
        super(x, y);
        vel = velocity;
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);

        Array<Boid> neighbors = matrix.getBoidNeighbors(getMatrixX(), getMatrixY());
        Vector3 alignmentForce = getAlignment(neighbors);
        Vector3 cohese = getCohesion(neighbors);
        Vector3 avoid = getAvoid(neighbors);
        float noiseX = 1 - (float) Math.random() * 2;
        float noiseY = 1 - (float) Math.random() * 2;
        Vector3 noise = new Vector3(noiseX * noiseFactor, noiseY * noiseFactor, 0);

        this.vel.add(alignmentForce.scl(1/10f));
        this.vel.add(cohese.scl(1/10f));
        this.vel.add(avoid.scl(1/5f));

        this.vel.limit(100);

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
        Vector3 formerLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
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
                if (xDiffIsLarger) {
                    if (thresholdPassed) {
                        vel.y *= -1;
                        vel.x *= -1;
                    } else {
                        vel.x *= -1;
                    }
                } else {
                    if (thresholdPassed) {
                        vel.y *= -1;
                        vel.x *= -1;
                    } else {
                        vel.x *= -1;
                    }
                }
                return;
            }
        }
    }

    private Vector3 getAvoid(Array<Boid> neighbors) {
        Vector3 steer = new Vector3();
        Vector3 location = new Vector3(getMatrixX(), getMatrixY(), 0);
        for (Boid neighbor : neighbors) {
            Vector3 otherLocationn = new Vector3(neighbor.getMatrixX(), neighbor.getMatrixY(), 0);
            float distance = location.dst(otherLocationn);

            Vector3 diff = location.cpy().sub(otherLocationn);
            diff.nor();
            diff.scl(1/ distance);
            steer.add(diff);
        }
        return steer;
    }

    private Vector3 getCohesion(Array<Boid> neighbors) {
        int totalX = 0;
        int totalY = 0;
        for (Boid neighbor : neighbors) {
            totalX += neighbor.getMatrixX();
            totalY += neighbor.getMatrixY();
        }
        if (neighbors.size > 0) {
            float avgX = totalX / (float)  neighbors.size;
            float avgY = totalY / (float)  neighbors.size;
            float desiredX = avgX - this.getMatrixX();
            float desiredY = avgY - this.getMatrixY();
            return new Vector3(desiredX, desiredY, 0);
        } else {
            return new Vector3();
        }
    }

    private Vector3 getAlignment(Array<Boid> neighbors) {
        Vector3 avg = new Vector3(0,0,0);
        if (neighbors.size == 0) return avg;
        for (Boid neighbor : neighbors) {
            avg.add(neighbor.vel);
        }
        avg.scl(1f/(float) neighbors.size);
        return avg;
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell || neighbor instanceof Boid) {
            if (isFinal) {
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
                return false;
            }
        } else if (neighbor instanceof Solid || neighbor instanceof Liquid) {
            vel.scl(-1);
            return true;
        }
        return false;
    }
}
