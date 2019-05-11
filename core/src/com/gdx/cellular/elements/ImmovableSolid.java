package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;

public abstract class ImmovableSolid extends Solid {

    public ImmovableSolid(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
    }

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
    }

    @Override
    public void step(CellularMatrix matrix) {

    }
}
