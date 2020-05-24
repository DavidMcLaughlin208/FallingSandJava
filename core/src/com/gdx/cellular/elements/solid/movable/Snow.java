package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;

public class Snow extends MovableSolid {

    public Snow(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -62f,0f);
        frictionFactor = .4f;
        inertialResistance = .8f;
        mass = 200;
        flammabilityResistance = 100;
        resetFlammabilityResistance = 35;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        if (heat > 0) {
            dieAndReplace(matrix, ElementType.WATER);
            return true;
        }
        return false;
    }

    @Override
    public void step(CellularMatrix matrix) {
        super.step(matrix);
        if (vel.y < -62) {
            vel.y = Math.random() > 0.3 ? -62 : -124;
        }
    }

}
