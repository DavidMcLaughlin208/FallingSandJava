package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.liquid.Liquid;

public class Spark extends Gas {

    public Spark(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 10;
        frictionFactor = 1f;
        density = 4;
        dispersionRate = 4;
        color = Color.RED;
        defaultColor = Color.RED;
        flammabilityResistance = 25;
        isIgnited = true;
        lifeSpan = (int) (Math.random() * 20);
        temperature = 3;
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);

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
                return;
            }
            lifeSpan -= 1;
        }
        modifyColor();
        checkLifeSpan(matrix);
    }

    private void checkLifeSpan(CellularMatrix matrix) {
        if (lifeSpan <= 0) {
            die(matrix);
        }
    }

    private boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        boolean acted = actOnOther(neighbor, matrix);
        if (acted) return true;
        if (neighbor instanceof EmptyCell) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Spark) {
            return false;
        } else if (neighbor instanceof Liquid || neighbor instanceof Solid || neighbor instanceof Gas) {
            neighbor.applyHeat(heatFactor);
            die(matrix);
            return true;
        }
        return false;
    }


}
