package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Water extends Liquid {

    public Water(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        mass = 0;
        frictionFactor = 1f;
        color = Color.BLUE;
    }
}
