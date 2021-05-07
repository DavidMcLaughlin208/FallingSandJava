package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Dirt extends MovableSolid {

    public Dirt(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .6f;
        inertialResistance = .4f;
        mass = 200;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

}
