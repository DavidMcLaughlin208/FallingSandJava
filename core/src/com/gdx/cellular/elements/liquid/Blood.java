package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Blood extends Liquid{

    public Blood(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 6;
        dispersionRate = 5;
        color = Color.RED;
        defaultColor = Color.RED;
        coolingFactor = 5;
    }

}
