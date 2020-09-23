package com.gdx.cellular.input.processors;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.input.InputElement;
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.input.InputProcessors;


public class CreatorInputProcessor implements InputProcessor {

    private final InputManager inputManager;
    private final OrthographicCamera camera;
    private final CellularMatrix matrix;
    private final InputProcessors parent;

    public CreatorInputProcessor(InputProcessors inputProcessors, InputManager inputManager, OrthographicCamera camera, CellularMatrix matrix) {
        this.parent = inputProcessors;
        this.inputManager = inputManager;
        this.camera = camera;
        this.matrix = matrix;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.EQUALS) {
            inputManager.calculateNewBrushSize(2);
        }
        if (keycode == Input.Keys.MINUS) {
            inputManager.calculateNewBrushSize(-2);
        }
        ElementType elementType = InputElement.getElementForKeycode(keycode);
        if (elementType != null) {
            inputManager.setCurrentlySelectedElement(elementType);
        }
        if (keycode == Input.Keys.SPACE) {
            inputManager.placeSpout(matrix);
        }
        if (keycode == Input.Keys.C) {
            inputManager.clearMatrix(matrix);
            inputManager.clearBox2dActors();
        }
        if (keycode == Input.Keys.P) {
            inputManager.togglePause();
        }
        if (keycode == Input.Keys.M) {
            inputManager.cycleBrushType();
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        inputManager.calculateNewBrushSize(amount * -2);
        return true;
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
        if (button == Input.Buttons.LEFT && !inputManager.drawMenu) {
            inputManager.spawnElementByInput(matrix);
        } else if (button == Input.Buttons.RIGHT) {
            inputManager.setTouchedLastFrame(false);
            Vector3 pos = camera.unproject(new Vector3(screenX, screenY, 0));
            inputManager.setDrawMenuAndLocation(pos.x, pos.y);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            inputManager.setTouchedLastFrame(false);
            inputManager.touchUpLMB(matrix);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inputManager.spawnElementByInput(matrix);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

}
