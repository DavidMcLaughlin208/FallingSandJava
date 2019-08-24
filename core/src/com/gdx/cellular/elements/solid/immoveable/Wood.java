package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;

public class Wood extends ImmovableSolid {

    public Wood(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        color = Color.BROWN;
        defaultColor = Color.BROWN;
        inertialResistance = 1.1f;
        mass = 500;
        health = getRandomInt(100) + 250;
    }

    @Override
    public void checkIfDead(CellularMatrix matrix) {
        if (this.health <= 0) {
            if (isIgnited && Math.random() > .9f) {
                dieAndReplace(matrix, ElementType.EMBER);
            } else {
                die(matrix);
            }
        }
    }
}
