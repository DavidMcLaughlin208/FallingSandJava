package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Ember extends MovableSolid {

    public Ember(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .9f;
        inertialResistance = .99f;
        mass = 200;
        isIgnited = true;
        health = getRandomInt(100) + 250;
        temperature = 5;
        flammabilityResistance = 0;
        resetFlammabilityResistance = 20;
    }
}
