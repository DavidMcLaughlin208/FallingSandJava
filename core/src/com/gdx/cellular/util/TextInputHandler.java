package com.gdx.cellular.util;

import com.badlogic.gdx.Input;
import com.gdx.cellular.InputManager;

public class TextInputHandler implements Input.TextInputListener {
    private InputManager inputManager;

    public TextInputHandler(InputManager inputManager) {
        this.inputManager = inputManager;

    }

    @Override
    public void input (String text) {
        String sane = text.replaceAll("[^a-zA-Z0-9\\._]+", "_");
        inputManager.setFileNameForSave(sane);
    }

    @Override
    public void canceled () {
        inputManager.setIsPaused(false);
    }
}
