package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;

public class Blood extends Liquid {

    public Blood(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 6;
        dispersionRate = 5;
        coolingFactor = 5;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix) {
        if (other.shouldApplyHeat()) {
            other.receiveCooling(matrix, coolingFactor);
            coolingFactor--;
            if (coolingFactor <= 0) {
                die(matrix);
                return true;
            }
            return false;
        }
        return false;
    }

}
