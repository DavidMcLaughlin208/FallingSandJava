package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class Lava extends Liquid {

    public Lava(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 10;
        dispersionRate = 1;
        temperature = 10;
        heated = true;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }

    @Override
    public void checkIfDead(CellularMatrix matrix) {
        if (this.temperature <= 0) {
            dieAndReplace(matrix, ElementType.STONE);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0) continue;
                    Element element = matrix.get(this.matrixX + x, this.matrixY + y);
                    if (element instanceof Liquid) {
                        element.dieAndReplace(matrix, ElementType.STONE);
                    }
                }
            }
        }
        if (this.health <= 0) {
            die(matrix);
        }
    }

    @Override
    public boolean receiveCooling(CellularMatrix matrix, int cooling) {
        this.temperature -= cooling;
        checkIfDead(matrix);
        return true;
    }
}
