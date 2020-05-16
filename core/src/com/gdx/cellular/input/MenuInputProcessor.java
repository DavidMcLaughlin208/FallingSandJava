package com.gdx.cellular.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;

public class MenuInputProcessor implements InputProcessor {


    private boolean showMenu = false;

    private final InputManager inputManager;
    private final OrthographicCamera camera;
    private final CellularMatrix matrix;
    private final InputProcessors parent;


    public MenuInputProcessor(InputProcessors inputProcessors, InputManager inputManager, OrthographicCamera camera, CellularMatrix matrix) {
        super();
        this.parent = inputProcessors;
        this.inputManager = inputManager;
        this.camera = camera;
        this.matrix = matrix;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            Vector3 pos = camera.unproject(new Vector3(screenX, screenY, 0));
            inputManager.setDrawMenuAndLocation(pos.x, pos.y);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
