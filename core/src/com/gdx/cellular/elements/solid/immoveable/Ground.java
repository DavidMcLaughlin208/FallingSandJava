package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ColorConstants;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;

public class Ground extends ImmovableSolid{

    public Ground(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 200;
        health = 250;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

    @Override
    public void customElementFunctions(CellularMatrix matrix) {
//        Element above = matrix.get(matrixX, matrixY + 1);
//        if (above == null || above instanceof EmptyCell) {
//            this.color = ColorConstants.getColorByName("Grass");
//        } else {
//            this.color = ColorConstants.getColorForElementType(this.elementType);
//        }
    }

}
