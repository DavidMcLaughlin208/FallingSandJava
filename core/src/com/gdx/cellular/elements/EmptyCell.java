package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

}
