package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;

public class Sand extends MovableSolid {

    public Sand(Cell cell) {
        super(cell);
        vel = new Vector3(Math.random() > 0.5 ? -1 : 1, -124f,0f);
        frictionFactor = 0.9f;
        inertialResistance = .1f;
        mass = 150;
        color = Color.YELLOW;
    }

}
