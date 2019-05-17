package com.gdx.cellular;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.ElementType;

public class InputManager {

    private int maxBrushSize = 55;
    private int minBrushSize = 1;
    private int brushIncrements = 2;

    private int maxThreads = 50;

    private Vector3 lastTouchPos = new Vector3();
    private boolean touchedLastFrame = false;

    private boolean paused = false;

    public ElementType getNewlySelectedElementWithDefault(ElementType defaultElement) {
        ElementType elementType = defaultElement;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            elementType = ElementType.STONE;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            elementType = ElementType.SAND;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            elementType = ElementType.DIRT;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            elementType = ElementType.WATER;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            elementType = ElementType.OIL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            elementType = ElementType.EMPTY_CELL;
        }
        return elementType;
    }

    public int calculateNewBrushSize(int currentSize) {
        int newSize = currentSize;
        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            newSize = Math.min(maxBrushSize, newSize + brushIncrements);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            newSize = Math.max(minBrushSize, newSize - brushIncrements);
        }
        return newSize;
    }

    public int adjustThreadCount(int numThreads) {
        int newThreads = numThreads;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            newThreads += numThreads == maxThreads ? 0 : 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            newThreads -= numThreads == 1 ? 0 : 1;
        }
        return newThreads;
    }

    public boolean toggleThreads(boolean toggleThreads) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            return !toggleThreads;
        } else {
            return toggleThreads;
        }
    }

    public void clearMatrixIfInput(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            matrix.clearAll();
        }
    }

    public void placeSpout(CellularMatrix matrix, OrthographicCamera camera, ElementType currentlySelectedElement, int brushSize) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize);
        }
    }

    public void spawnElementByInput(CellularMatrix matrix, OrthographicCamera camera, ElementType currentlySelectedElement, int brushSize) {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (touchedLastFrame) {
                matrix.iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
            } else {
                matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
            }
            lastTouchPos = touchPos;
            touchedLastFrame = true;
        } else {
            touchedLastFrame = false;
        }
    }


    public boolean getIsPaused() {
        boolean stepOneFrame = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            stepOneFrame = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
        }
        return paused && !stepOneFrame;
    }
}
