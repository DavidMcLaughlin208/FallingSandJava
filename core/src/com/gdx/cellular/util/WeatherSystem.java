package com.gdx.cellular.util;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class WeatherSystem {

    public ElementType elementType;
    public int weight;
    public boolean disabled = true;

    public WeatherSystem(ElementType elementType, int weight) {
        this.elementType = elementType;
        this.weight = weight;
    }

    public void enact(CellularMatrix matrix) {
        if (disabled) {
            return;
        }
        int numberToEmit = weight;
        for (int i = 0; i < numberToEmit; i++) {
            int x = (int) (Math.random() * (matrix.innerArraySize - 1));
            Element newElement = elementType.createElementByMatrix(x, matrix.outerArraySize - 1);
            newElement.vel = new Vector3(30, -256, 0);
            matrix.setElementAtIndex(x, matrix.outerArraySize - 1, newElement);
            matrix.reportToChunkActive(x, matrix.outerArraySize - 1);
        }
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            this.weight = 0;
            return;
        } else if (weight > 20) {
            this.weight = 20;
            return;
        }
        this.weight = weight;
    }

    public void enable() {
        this.disabled = false;
    }

    public void disable() {
        this.disabled = true;
    }


    public void toggle() {
        this.disabled = !this.disabled;
    }
}
