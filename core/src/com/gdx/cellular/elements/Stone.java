package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;

public class Stone extends Element implements Solid {

    public Stone(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
    }

    public Color color = Color.GRAY;

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
    }

    @Override
    public void step(Array<Array<Element>> matrix) {

    }


}
