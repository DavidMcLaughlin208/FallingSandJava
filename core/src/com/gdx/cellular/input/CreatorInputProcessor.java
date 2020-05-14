package com.gdx.cellular.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.InputManager;
import com.gdx.cellular.elements.ElementType;

import static com.gdx.cellular.MouseMode.RECTANGLE;

public class CreatorInputProcessor implements InputProcessor {

    private final InputManager inputManager;
    private final OrthographicCamera camera;
    private final CellularMatrix matrix;

    public CreatorInputProcessor(InputManager inputManager, OrthographicCamera camera, CellularMatrix matrix) {
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
//            if (inputManager.getMouseMode() == RECTANGLE) {
//                inputManager.drawRect(matrix, camera);
//            } else {
                inputManager.spawnElementByInput(matrix, camera);
//            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            inputManager.setTouchedLastFrame(false);
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
