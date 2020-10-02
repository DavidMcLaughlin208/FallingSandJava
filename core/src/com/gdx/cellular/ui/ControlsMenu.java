package com.gdx.cellular.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.cellular.input.InputManager;

public class ControlsMenu {

    private final InputManager inputManager;
    private final Viewport viewport;

    public ControlsMenu(InputManager inputManager, Viewport viewport) {
        this.inputManager = inputManager;
        this.viewport = viewport;
        createMenu(viewport);
    }

    private void createMenu(Viewport viewport) {
        Stage stage = new Stage(viewport);
        Skin skin = Skins.getSkin("uiskin");
    }


}
