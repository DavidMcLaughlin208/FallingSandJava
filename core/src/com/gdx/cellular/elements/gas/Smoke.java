package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.Solid;

public class Smoke extends Gas{

    public Smoke(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        frictionFactor = 1f;
        density = 1;
        dispersionRate = 4;
        color = Color.WHITE;
        defaultColor = Color.WHITE;
        lifeSpan = getRandomInt(250) + 450;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }
}
