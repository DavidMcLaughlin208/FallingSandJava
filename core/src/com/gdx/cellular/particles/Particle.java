package com.gdx.cellular.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ColorConstants;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.Solid;

public class Particle extends Element {

    public ElementType containedElementType;

    public Particle(int x, int y, boolean isPixel, Vector3 vel, ElementType elementType) {
        super(x, y, isPixel);
        if (ElementType.PARTICLE.equals(elementType)) {
            throw new IllegalStateException("Containing element cannot be particle");
        }
        this.containedElementType = elementType;
        this.vel = new Vector3();
        Vector3 localVel = vel == null ? new Vector3(0, -124, 0) : vel;
        this.vel.x = localVel.x;
        this.vel.y = localVel.y;
        this.color = ColorConstants.getColorForElementType(elementType);
    }

    @Override
    public void dieAndReplace(CellularMatrix matrix, ElementType elementType) {
        particleDeathAndSpawn(matrix);
    }

    private void particleDeathAndSpawn(CellularMatrix matrix) {
        if (matrix.get(this.matrixX, this.matrixY) instanceof EmptyCell) {
            die(matrix, elementType);
        } else {
            int yIndex = 0;
            while (true) {
                Element elementAtNewPos = matrix.get(matrixX, matrixY + yIndex);
                if (elementAtNewPos == null) {
                    break;
                } else if (elementAtNewPos instanceof EmptyCell) {
                    die(matrix);
                    matrix.setElementAtIndex(matrixX, matrixY + yIndex, containedElementType.createElementByMatrix(matrixX, matrixY + yIndex));
                    matrix.reportToChunkActive(matrixX, matrixY + yIndex);
                    break;
                }
                yIndex++;
            }
        }
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.add(CellularAutomaton.gravity);
        if (vel.y > -64 && vel.y < 32) {
            vel.y = -64;
        }

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
                boolean stopped = actOnNeighboringElement(neighbor, modifiedMatrixX, modifiedMatrixY, matrix, i == upperBound, i == 1, lastValidLocation, 0);
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
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            if (isFinal) {
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid || neighbor instanceof Solid) {
            moveToLastValid(matrix, lastValidLocation);
            dieAndReplace(matrix, containedElementType);
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
}
