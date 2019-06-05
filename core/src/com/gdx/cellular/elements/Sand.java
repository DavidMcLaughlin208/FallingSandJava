package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Sand extends MovableSolid {

    public Sand(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(Math.random() > 0.5 ? -1 : 1, -124f,0f);
        frictionFactor = 0.9f;
        inertialResistance = .1f;
        mass = 150;
        color = Color.YELLOW;
    }

    @Override
    public boolean applyHeat(int heat) {
        return false;
    }

}
