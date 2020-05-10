package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;

public class Stone extends ImmovableSolid {

    public Stone(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 500;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }

}
