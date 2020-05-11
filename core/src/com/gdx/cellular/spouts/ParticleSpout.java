package com.gdx.cellular.spouts;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix.FunctionInput;
import com.gdx.cellular.elements.ElementType;


import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ParticleSpout implements Spout {

    int matrixX;
    int matrixY;
    ElementType sourceElement;
    int brushSize;
    Consumer<FunctionInput> function;
    
    public ParticleSpout(ElementType sourceElement, int matrixX, int matrixY, int brushSize, Consumer<FunctionInput> function) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.sourceElement = sourceElement;
        this.brushSize = brushSize;
        this.function = function;
    }

    @Override
    public FunctionInput setFunctionInputs(FunctionInput functionInput) {
        functionInput.setInput(FunctionInput.X, matrixX);
        functionInput.setInput(FunctionInput.Y, matrixY);
        functionInput.setInput(FunctionInput.BRUSH_SIZE, brushSize);
        functionInput.setInput(FunctionInput.ELEMENT_TYPE, sourceElement);
        functionInput.setInput(FunctionInput.VELOCITY, generateRandomVelocity());
        return functionInput;
    }

    @Override
    public Consumer<FunctionInput> getFunction() {
        return function;
    }

    private Vector3 generateRandomVelocity() {
        int x = ThreadLocalRandom.current().nextInt(-500, 500);
        int y = ThreadLocalRandom.current().nextInt(-500, 500);
        return new Vector3( x, y, 0);
    }
}
