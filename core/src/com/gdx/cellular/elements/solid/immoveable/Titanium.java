package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Titanium extends ImmovableSolid {

    public Titanium(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 1000;
        explosionResistance = 5;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public boolean infect(CellularMatrix matrix) {
        return false;
    }
}
