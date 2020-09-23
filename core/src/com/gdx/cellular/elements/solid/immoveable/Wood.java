package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;

public class Wood extends ImmovableSolid {

    public Wood(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 500;
        health = getRandomInt(100) + 100;
        flammabilityResistance = 40;
        resetFlammabilityResistance = 25;
    }

    @Override
    public void checkIfDead(CellularMatrix matrix) {
        if (this.health <= 0) {
            if (isIgnited && Math.random() > .95f) {
                dieAndReplace(matrix, ElementType.EMBER);
            } else {
                die(matrix);
            }
        }
    }
}
