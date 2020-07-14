package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;

public class Steam extends Gas {

    public Steam(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        frictionFactor = 1f;
        density = 5;
        dispersionRate = 2;
        lifeSpan = getRandomInt(2000) + 1000;
    }

    @Override
    public void checkLifeSpan(CellularMatrix matrix) {
        if (lifeSpan != null) {
            lifeSpan--;
            if (lifeSpan <= 0) {
                if (Math.random() > 0.5) {
                    die(matrix);
                } else {
                    dieAndReplace(matrix, ElementType.WATER);
                }
            }
        }
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }
}
