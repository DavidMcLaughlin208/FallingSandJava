package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.gdx.cellular.input.Cursor;

public class CursorActor extends Actor {

    private ShapeRenderer shapeRenderer;
    public Cursor cursor;

    public CursorActor(ShapeRenderer shapeRenderer, Cursor cursor) {
        this.shapeRenderer = shapeRenderer;
        this.cursor = cursor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        cursor.draw(shapeRenderer);
        batch.begin();
    }

}
