package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;

public class Water extends Liquid {

    public Water(Cell cell) {
        super(cell);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 5;
        dispersionRate = 4;
        color = Color.BLUE;
    }
}
