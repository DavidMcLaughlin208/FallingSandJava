package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class Acid extends Liquid {

    public int corrosionCount = 3;
    public Acid(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 50;
        frictionFactor = 1f;
        density = 2;
        dispersionRate = 2;
        color = Color.GREEN;
        defaultColor = Color.GREEN;
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix) {
        if (!isReactionFrame() || other == null) return false;
        boolean corroded = other.corrode(matrix);
        if (corroded) corrosionCount -= 1;
        if (corrosionCount <= 0) {
            dieAndReplace(matrix, ElementType.FLAMMABLEGAS);
            return true;
        }
        return false;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }
}
