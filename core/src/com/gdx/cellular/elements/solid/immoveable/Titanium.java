package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.EffectColors;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;

public class Titanium extends ImmovableSolid {

    public Titanium(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        color = EffectColors.TITANIUM_COLOR;
        defaultColor = EffectColors.TITANIUM_COLOR;
        inertialResistance = 1.1f;
        mass = 1000;
    }

    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }
}
