package com.gdx.cellular.util;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

public class WeatherSystem {

    public ElementType elementType;
    public int weight;
    public CellularMatrix matrix;

    public WeatherSystem(ElementType elementType, int weight, CellularMatrix matrix) {
        this.elementType = elementType;
        this.weight = weight;
        this.matrix = matrix;
    }

    public void enact() {
        int numberToEmit = weight;
        for (int i = 0; i < numberToEmit; i++) {
            int x = (int) (Math.random() * (matrix.innerArraySize - 1));
            Element newElement = elementType.createElementByMatrix(x, matrix.outerArraySize - 1);
            newElement.vel = new Vector3(32, -256, 0);
            matrix.setElementAtIndex(x, matrix.outerArraySize - 1, newElement);
            matrix.reportToChunkActive(x, matrix.outerArraySize - 1);
        }
    }


}
