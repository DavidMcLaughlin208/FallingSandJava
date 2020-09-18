package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ModeActor extends Actor {

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
//        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
//        cursor.draw(shapeRenderer);
        batch.begin();
    }
}
