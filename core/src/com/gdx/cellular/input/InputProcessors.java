package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.InputManager;

public class InputProcessors {

    private final InputManager inputManager;
    private final InputProcessor creatorInputProcessor;
    private final InputProcessor menuInputProcessor;

    public InputProcessors(InputManager inputManager, CellularMatrix matrix, OrthographicCamera camera) {
        this.inputManager = inputManager;
        this.menuInputProcessor = new MenuInputProcessor(this, inputManager, camera, matrix);
        this.creatorInputProcessor = new CreatorInputProcessor(this, inputManager, camera, matrix);
        this.inputManager.setCreatorInputProcessor(creatorInputProcessor);
        Gdx.input.setInputProcessor(creatorInputProcessor);
    }

}
