package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularMatrix;

public class EmptyCell extends Element {

    public EmptyCell(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        color = Color.CLEAR;
    }

//    @Override
//    public void draw(ShapeRenderer sr) {}

    @Override
    public void step(CellularMatrix matrix) {

    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        return true;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }


}
