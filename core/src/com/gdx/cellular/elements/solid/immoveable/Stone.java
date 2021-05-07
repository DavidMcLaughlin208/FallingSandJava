package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Stone extends ImmovableSolid {

    public Stone(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 500;
        explosionResistance = 100;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

}
