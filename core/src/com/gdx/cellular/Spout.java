package com.gdx.cellular;

import com.gdx.cellular.elements.ElementType;

public class Spout {

    int matrixX;
    int matrixY;
    ElementType sourceElement;
    int brushSize;
    
    public Spout(ElementType sourceElement, int matrixX, int matrixY, int brushSize) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.sourceElement = sourceElement;
        this.brushSize = brushSize;
    }
}
