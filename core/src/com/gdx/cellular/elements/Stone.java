package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;

public class Stone extends ImmovableSolid {

    public Stone(Cell cell) {
        super(cell);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        color = Color.GRAY;
        inertialResistance = 1.1f;
        mass = 500;
    }

}
