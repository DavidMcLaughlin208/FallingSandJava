package com.gdx.cellular.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.gdx.cellular.input.InputManager;

public class ModeActor extends Actor {

    public InputManager inputManager;
    public Skin skin;
    public Label modeLabel;
    public Label elementLabel;
    public Label weatherLabel;
    public int pixelX;
    public int pixelY;

    public ModeActor(InputManager inputManager, int x, int y) {
        this.inputManager = inputManager;
        this.skin = Skins.getSkin("uiskin");
        this.modeLabel = new Label(inputManager.getMouseMode().toString(), skin);
        this.elementLabel = new Label(inputManager.currentlySelectedElement.toString(), skin);
        this.weatherLabel = new Label("Weather", skin);
        this.pixelX = x;
        this.pixelY = y;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.modeLabel.setText("Current Mode: " + this.inputManager.getMouseMode().toString());
        this.modeLabel.setX(pixelX);
        this.modeLabel.setY(pixelY);
        this.modeLabel.draw(batch, 1);
        this.elementLabel.setText("Current Element: " + this.inputManager.currentlySelectedElement.toString());
        this.elementLabel.setX(this.pixelX);
        this.elementLabel.setY(this.pixelY - this.modeLabel.getHeight()/1.5f);
        this.elementLabel.draw(batch, 1);
        String weatherString = this.inputManager.weatherSystem.disabled ? "OFF" : "ON";
        this.weatherLabel.setText("Weather: " + weatherString + "  Element: " + this.inputManager.weatherSystem.elementType.toString());
        this.weatherLabel.setX(this.pixelX);
        this.weatherLabel.setY(this.pixelY - (this.modeLabel.getHeight()/1.5f) * 2);
        this.weatherLabel.draw(batch, 1);
    }
}
