package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Smoke extends Gas{

    public Smoke(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        frictionFactor = 1f;
        density = 2;
        dispersionRate = 2;
        lifeSpan = getRandomInt(250) + 450;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }
}
