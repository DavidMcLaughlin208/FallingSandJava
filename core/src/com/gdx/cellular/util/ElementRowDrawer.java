package com.gdx.cellular.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gdx.cellular.CellularMatrix;

public class ElementRowDrawer implements Runnable {

    public CellularMatrix matrix;
    public int minRow;
    public int maxRow;
    public ShapeRenderer sr;

    public ElementRowDrawer(CellularMatrix matrix, int minRow, int maxRow, ShapeRenderer sr) {
        this.matrix = matrix;
        this.minRow = minRow;
        this.maxRow = maxRow;
        this.sr = sr;
    }

    @Override
    public void run() {
        matrix.drawProvidedRows(minRow, maxRow, sr);
    }
}
