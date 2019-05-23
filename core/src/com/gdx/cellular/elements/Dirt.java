package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;

public class Dirt extends MovableSolid {

    public Dirt(Cell cell) {
        super(cell);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .6f;
        inertialResistance = .8f;
        mass = 200;
        color = Color.BROWN;
    }

}
