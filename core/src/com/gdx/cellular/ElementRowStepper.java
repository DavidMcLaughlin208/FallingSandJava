package com.gdx.cellular;

public class ElementRowStepper implements Runnable {

    public int minRow;
    public int maxRow;
    public CellularMatrix matrix;

    public ElementRowStepper(int minRow, int maxRow, CellularMatrix matrix) {
        this.minRow = minRow;
        this.maxRow = maxRow;
        this.matrix = matrix;
    }

    @Override
    public void run() {
        matrix.stepProvidedRows(minRow, maxRow);
    }
}
