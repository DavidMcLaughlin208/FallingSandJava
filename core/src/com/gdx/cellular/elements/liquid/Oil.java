package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Oil extends Liquid {

    public Oil(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 75;
        frictionFactor = 1f;
        density = 4;
        dispersionRate = 4;
        color = Color.DARK_GRAY;
        defaultColor = Color.DARK_GRAY;
        flammabilityResistance = 15;
        resetFlammabilityResistance = 5;
        fireDamage = 10;
        temperature = 10;
        health = 1000;
    }
}
