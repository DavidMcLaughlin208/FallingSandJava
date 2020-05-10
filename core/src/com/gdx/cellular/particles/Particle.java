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

import java.util.Optional;

public class Particle extends Element {

    public ElementType containedElementType;

    public Particle(int x, int y, boolean isPixel, Vector3 vel, ElementType elementType) {
        super(x, y, isPixel);
        this.containedElementType = elementType;
        this.vel = Optional.ofNullable(vel).orElse(new Vector3(0, -124f, 0));
        this.color = ColorConstants.getColorForElementType(elementType);
    }

    @Override
    public void dieAndReplace(CellularMatrix matrix, ElementType elementType) {
        die(matrix, containedElementType);
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.add(CellularAutomaton.gravity);
        if (vel.y > -64 && vel.y < 64) {
            vel.y = -64;
        }

//        vel.y = vel.y < 0 ? Math.max(-124, vel.y) : vel.y;

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
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid || neighbor instanceof Solid) {
            moveToLastValidDieAndReplace(matrix, lastValidLocation, containedElementType);
            return true;
        } else if (neighbor instanceof Gas) {
            if (isFinal) {
                moveToLastValidAndSwap(matrix, neighbor, lastValidLocation);
                return true;
            }
            return false;
        }
        return false;
    }
}
