package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class Cement extends Liquid {

    public Cement(int x, int y) {
        super(x, y);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 9;
        dispersionRate = 1;
        coolingFactor = 5;
        stoppedMovingThreshold = 900;
    }

    @Override
    public void step(CellularMatrix matrix) {
        super.step(matrix);
        if (stoppedMovingCount >= stoppedMovingThreshold) {
            dieAndReplace(matrix, ElementType.STONE);
        }
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix) {
        return false;
    }
}
