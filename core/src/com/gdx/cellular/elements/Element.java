package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.Cell;
import com.gdx.cellular.CellularMatrix;

import java.util.BitSet;

public abstract class Element {

    public Vector3 vel;

    public float frictionFactor;
    public boolean isFreeFalling = true;
    public float inertialResistance;
    public int mass;

    public Cell outerCell;

    public Color color;

    public BitSet stepped = new BitSet(1);

    public Element(Cell outerCell) {
        this.outerCell = outerCell;
    }

    //    public abstract void draw(ShapeRenderer sr);

    public abstract void step(CellularMatrix matrix);

    public int getMatrixX() {
        return (int) this.outerCell.matrixLocation.x;
    }

    public int getMatrixY() {
        return (int) this.outerCell.matrixLocation.y;
    }

    public float getPixelX() {
        return (int) this.outerCell.pixelLocation.x;
    }

    public float getPixelY() {
        return (int) this.outerCell.pixelLocation.y;
    }

}
