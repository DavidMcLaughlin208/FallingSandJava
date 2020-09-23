package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.gdx.cellular.input.InputManager;

public class ModeActor extends Actor {

    public InputManager inputManager;
    public ModeUI modeUI;

    public ModeActor(InputManager inputManager, ModeUI modeUI) {
        this.inputManager = inputManager;
        this.modeUI = modeUI;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        this.modeUI.draw();
        batch.begin();
    }
}
