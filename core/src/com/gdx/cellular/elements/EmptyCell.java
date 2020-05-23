package com.gdx.cellular.elements;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class EmptyCell extends Element {
    private static Element element;

    private EmptyCell(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
    }

    public static Element getInstance() {
        if (element == null) {
            element = new EmptyCell(-1, -1, false);
        }
        return element;
    }

    @Override
    public void step(CellularMatrix matrix) {

    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        return true;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

    @Override
    public void setCoordinatesByMatrix(Vector2 pos) { }

    @Override
    public void setCoordinatesByMatrix(int providedX, int providedY) { }

    @Override
    public void setSecondaryCoordinatesByMatrix(int providedX, int providedY) { }

    @Override
    public void setCoordinatesByPixel(int providedX, int providedY) { }

    @Override
    public void setXByPixel(int providedVal) { }

    @Override
    public void setYByPixel(int providedVal) { }

    @Override
    public void setXByMatrix(int providedVal) { }

    @Override
    public void setYByMatrix(int providedVal) { }


}
