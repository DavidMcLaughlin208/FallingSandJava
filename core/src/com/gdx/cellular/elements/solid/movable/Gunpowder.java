package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class Gunpowder extends MovableSolid {

    private int ignitedCount = 0;
    private final int ignitedThreshold = 7;

    public Gunpowder(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .4f;
        inertialResistance = .8f;
        mass = 200;
        flammabilityResistance = 10;
        resetFlammabilityResistance = 35;
        explosionRadius = 15;
        fireDamage = 3;
    }

    public void step(CellularMatrix matrix) {
        super.step(matrix);
        if (isIgnited) {
            ignitedCount++;
        }
        if (ignitedCount >= ignitedThreshold) {
            matrix.addExplosion(15, 10, this);
        }
    }

}
