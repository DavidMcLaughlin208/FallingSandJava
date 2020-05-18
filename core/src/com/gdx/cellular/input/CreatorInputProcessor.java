package com.gdx.cellular.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;

import static com.gdx.cellular.input.MouseMode.RECTANGLE;


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
            inputManager.placeSpout(matrix, camera);
        }
        if (keycode == Input.Keys.C) {
            inputManager.clearMatrix(matrix);
            inputManager.clearBox2dActors();
        }
        if (keycode == Input.Keys.P) {
            inputManager.togglePause();
        }
        if (keycode == Input.Keys.M) {
            inputManager.cycleMouseModes();
        }
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
        if (button == Input.Buttons.LEFT) {
            inputManager.spawnElementByInput(matrix, camera);
        } else if (button == Input.Buttons.RIGHT) {
            Vector3 pos = camera.unproject(new Vector3(screenX, screenY, 0));
            inputManager.setDrawMenuAndLocation(pos.x, pos.y);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            inputManager.setTouchedLastFrame(false);
            if (inputManager.getMouseMode() == RECTANGLE) {
                inputManager.spawnRect(matrix, camera);
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inputManager.spawnElementByInput(matrix, camera);
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
