package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.gdx.cellular.InputManager;

public class InputProcessors {

    private final InputManager inputManager;
    private final InputProcessor creatorInputProcessor;
    private final InputProcessor menuInputProcessor;

    public InputProcessors(InputManager inputManager) {
        this.inputManager = inputManager;
        this.menuInputProcessor = new MenuInputProcessor(inputManager);
        this.creatorInputProcessor = new CreatorInputProcessor(inputManager);
        Gdx.input.setInputProcessor(creatorInputProcessor);
    }

}
