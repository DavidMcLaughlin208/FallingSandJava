package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.input.processors.CreatorInputProcessor;
import com.gdx.cellular.input.processors.MenuInputProcessor;
import com.gdx.cellular.input.processors.PlayerInputProcessor;
import com.gdx.cellular.util.GameManager;

public class InputProcessors {

    private final InputManager inputManager;
    private final InputProcessor creatorInputProcessor;
    private final InputProcessor playerInputProcessor;

    public InputProcessors(InputManager inputManager, CellularMatrix matrix, OrthographicCamera camera, GameManager gameManager) {
        this.inputManager = inputManager;
        this.playerInputProcessor = new PlayerInputProcessor(this, gameManager);
        this.creatorInputProcessor = new CreatorInputProcessor(this, inputManager, camera, matrix);
        this.inputManager.setCreatorInputProcessor(creatorInputProcessor);
        Gdx.input.setInputProcessor(creatorInputProcessor);
    }

    public void setPlayerProcessor() {
        Gdx.input.setInputProcessor(playerInputProcessor);
    }

    public void setCreatorInputProcessor() {
        Gdx.input.setInputProcessor(creatorInputProcessor);
    }
}
