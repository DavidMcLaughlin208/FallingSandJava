package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.liquid.Liquid;

public class Spark extends Gas {

    public Spark(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 10;
        frictionFactor = 1f;
        density = 4;
        dispersionRate = 4;
        color = Color.RED;
        defaultColor = Color.RED;
        flammabilityResistance = 25;
        isIgnited = true;
        lifeSpan = getRandomInt(20);
        temperature = 3;
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        boolean acted = actOnOther(neighbor, matrix);
        if (acted) return true;
        if (neighbor instanceof EmptyCell) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Spark) {
            return false;
        } else if (neighbor instanceof Liquid || neighbor instanceof Solid || neighbor instanceof Gas) {
            neighbor.receiveHeat(heatFactor);
            die(matrix);
            return true;
        }
        return false;
    }


}
