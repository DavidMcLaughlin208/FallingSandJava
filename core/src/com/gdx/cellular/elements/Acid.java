package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Acid extends Liquid {
    public Acid(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 50;
        frictionFactor = 1f;
        density = 7;
        dispersionRate = 7;
        color = Color.GREEN;
    }
}
