package com.gdx.cellular.util;

import com.gdx.cellular.CellularMatrix;

public class ElementColumnStepper implements Runnable {


    public CellularMatrix matrix;
    public int colIndex;

    public ElementColumnStepper(CellularMatrix matrix, int colIndex) {
        this.matrix = matrix;
        this.colIndex = colIndex;
    }

    @Override
    public void run() {
        matrix.stepProvidedColumns(colIndex);
    }
}
