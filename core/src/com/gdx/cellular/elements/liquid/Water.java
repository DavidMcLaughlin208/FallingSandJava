package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class Water extends Liquid {

    public Water(int x, int y) {
        super(x, y);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 5;
        dispersionRate = 5;
        coolingFactor = 5;
        explosionResistance = 0;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        dieAndReplace(matrix, ElementType.STEAM);
        return true;
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix) {
        other.cleanColor();
        if (other.shouldApplyHeat()) {
            other.receiveCooling(matrix, coolingFactor);
            coolingFactor--;
            if (coolingFactor <= 0) {
                dieAndReplace(matrix, ElementType.STEAM);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean explode(CellularMatrix matrix, int strength) {
        if (explosionResistance < strength) {
            dieAndReplace(matrix, ElementType.STEAM);
            return true;
        } else {
            return false;
        }
    }


}
