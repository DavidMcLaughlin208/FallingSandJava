package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;

public class Oil extends Liquid {

    public Oil(Cell cell) {
        super(cell);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 75;
        frictionFactor = 1f;
        density = 3;
        dispersionRate = 2;
        color = Color.GOLD;
    }
}
