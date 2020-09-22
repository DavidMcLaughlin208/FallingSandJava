package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Gunpowder extends MovableSolid {

    public Gunpowder(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .4f;
        inertialResistance = .8f;
        mass = 200;
        flammabilityResistance = 100;
        resetFlammabilityResistance = 35;
        explosionRadius = 20;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        matrix.addExplosion(explosionRadius, 10, this.matrixX, this.matrixY);
        return true;
    }

}
