package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Wood extends ImmovableSolid {

    public Wood(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        color = Color.BROWN;
        inertialResistance = 1.1f;
        mass = 500;
    }
}
