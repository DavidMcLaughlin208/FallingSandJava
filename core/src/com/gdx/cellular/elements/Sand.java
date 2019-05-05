package com.gdx.cellular.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;

public class Sand extends Element implements Solid {

    public Sand(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -62f,0f);
    }

    public Color color = Color.YELLOW;

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
    }

    @Override
    public void step(Array<Array<Element>> matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.add(CellularAutomaton.gravity);
        int signFactor = vel.y < 0 ? -1 : 1;
        int velYMax = (int) (Math.abs(vel.y) * Gdx.graphics.getDeltaTime());
        for (int i = 1; i <= velYMax; i++) {
            int modifiedMatrixY = matrixY + (i * signFactor);
            if (modifiedMatrixY >= 0 && modifiedMatrixY < matrix.size) {
                Element neighbor = matrix.get(matrixY + (i * signFactor)).get(matrixX);
                if (neighbor == this) continue;
                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == velYMax, i == 1);
                if (stopped) {
                    break;
                }
            } else {
                matrix.get(matrixY).set(matrixX, new EmptyCell(pixelX, pixelY, true));
            }
        }
    }

    private boolean actOnNeighboringElement(Element neighbor, Array<Array<Element>> matrix, boolean isFinal, boolean isFirst) {
        if (neighbor instanceof EmptyCell) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            swapPositions(matrix, neighbor);
            return true;
        } else if (neighbor instanceof Solid) {
            if (isFirst) {
                Element diagnoalNeighbor = matrix.get(neighbor.matrixY).get(neighbor.matrixX + (Math.random() > .5 ? 1 : -1));
                actOnNeighboringElement(diagnoalNeighbor, matrix, true, false);
            }
            vel.y = -62f;
            return true;
        }
        return false;
    }
}
