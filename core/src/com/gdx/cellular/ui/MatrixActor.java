package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.util.ElementColumnStepper;
import com.gdx.cellular.util.ElementRowDrawer;

import java.util.ArrayList;
import java.util.List;

public class MatrixActor extends Actor {

    private final ShapeRenderer shapeRenderer;
    private final CellularMatrix matrix;
//    private final List<ShapeRenderer> shapeRenderers = new ArrayList<>();

    public MatrixActor(ShapeRenderer shapeRenderer, CellularMatrix matrix) {
        this.shapeRenderer = shapeRenderer;
        this.matrix = matrix;
//        for (int i = 0; i < matrix.drawThreadCount; i++) {
//            ShapeRenderer sr = new ShapeRenderer();
//            sr.setAutoShapeType(true);
//            shapeRenderers.add(sr);
//        }
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        matrix.drawAll(shapeRenderer);
//        int numThreads = matrix.drawThreadCount;
//        int rowsToDraw = matrix.outerArraySize / numThreads;
//        List<Thread> threads = new ArrayList<>(numThreads);
//        for (int t = 0; t < numThreads; t++) {
//            int minRow = t * (rowsToDraw + 1);
//            int maxRow = (t +1) * rowsToDraw;
//            if (t == numThreads - 1) {
//                maxRow = matrix.outerArraySize - 1;
//            }
//            Thread newThread = new Thread(new ElementRowDrawer(matrix, minRow, maxRow, shapeRenderers.get(t)));
//            threads.add(newThread);
//        }
//        for (Thread thread : threads) {
//            thread.start();
//        }
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        batch.begin();
    }
}
