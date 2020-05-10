package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;


public class Coal extends MovableSolid {

    public Coal(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .4f;
        inertialResistance = .8f;
        mass = 200;
        flammabilityResistance = 100;
        resetFlammabilityResistance = 35;
    }

    @Override
    public void spawnSparkIfIgnited(CellularMatrix matrix) {
        if (getRandomInt(20) > 2) return;
        super.spawnSparkIfIgnited(matrix);
    }
}
