package com.gdx.cellular;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MatrixActor extends Actor {

    private final ShapeRenderer shapeRenderer;
    private final CellularMatrix matrix;

    public MatrixActor(ShapeRenderer shapeRenderer, CellularMatrix matrix) {
        this.shapeRenderer = shapeRenderer;
        this.matrix = matrix;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        matrix.drawAll(shapeRenderer);
        batch.begin();
    }
}
