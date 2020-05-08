package com.gdx.cellular.util;

import com.badlogic.gdx.Input;
import com.gdx.cellular.InputManager;

import java.lang.reflect.Method;
import java.util.function.Function;

public class TextInputHandler implements Input.TextInputListener {
    private final Function<String, Boolean> function;
    private final InputManager inputManager;

    public TextInputHandler(InputManager inputManager, Function<String, Boolean> function) {
        this.inputManager = inputManager;
        this.function = function;

    }

    @Override
    public void input (String text) {
        String sanitizedInput = text.replaceAll("[^a-zA-Z0-9_]+", "_");
        function.apply(sanitizedInput);
    }

    @Override
    public void canceled () {
        inputManager.setIsPaused(false);
    }
}
