package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.gdx.cellular.input.InputManager;

public class ModeUI {

    public int pixelX;
    public int pixelY;
    public InputManager inputManager;
    public Skin skin;
    public Label modeLabel;
    public Label elementLabel;

    public ModeUI(int x, int y, InputManager inputManager) {
        this.pixelX = x;
        this.pixelY = y;
        this.skin = Skins.getSkin("uiskin");
        this.inputManager = inputManager;
        this.modeLabel = new Label(inputManager.getMouseMode().toString(), skin);
        this.elementLabel = new Label(inputManager.currentlySelectedElement.toString(), skin);
    }

    public void draw() {
        Batch batch = new SpriteBatch(1);
        batch.begin();
        this.modeLabel.setText("Current Mode: " + this.inputManager.getMouseMode().toString());
        this.modeLabel.setX(pixelX);
        this.modeLabel.setY(pixelY);
        this.modeLabel.draw(batch, 1);
        this.elementLabel.setText("Current Element: " + this.inputManager.currentlySelectedElement.toString());
        this.elementLabel.setX(this.pixelX);
        this.elementLabel.setY(this.pixelY - this.modeLabel.getHeight()/1.5f);
        this.elementLabel.draw(batch, 1);
        batch.end();
    }
}
