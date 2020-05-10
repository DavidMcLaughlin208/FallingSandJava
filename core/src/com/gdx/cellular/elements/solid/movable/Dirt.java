package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Dirt extends MovableSolid {

    public Dirt(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .6f;
        inertialResistance = .8f;
        mass = 200;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }

}
