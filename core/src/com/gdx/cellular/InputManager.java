package com.gdx.cellular;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputManager {

    private int maxBrushSize = 55;
    private int minBrushSize = 3;
    private int brushIncrements = 2;
    private MouseMode mouseMode = MouseMode.SPAWN;

    private int maxThreads = 50;

    private Vector3 lastTouchPos = new Vector3();
    private boolean touchedLastFrame = false;

    private boolean paused = false;
    private Path path = Paths.get("save/file.ser");

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
            elementType = ElementType.ACID;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
            elementType = ElementType.WOOD;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
            elementType = ElementType.TITANIUM;
        }  else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
            elementType = ElementType.EMPTY_CELL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            elementType = ElementType.SPARK;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            elementType = ElementType.EMBER;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            elementType = ElementType.LAVA;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            elementType = ElementType.COAL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            elementType = ElementType.FLAMMMABLEGAS;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            elementType = ElementType.BLOOD;
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

    public boolean cycleMouseModes() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (this.mouseMode == MouseMode.SPAWN) {
                this.mouseMode = MouseMode.HEAT;
            } else {
                this.mouseMode = MouseMode.SPAWN;
            }
            return true;
        }
        return false;
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
            if (mouseMode == MouseMode.SPAWN) {
                if (touchedLastFrame) {
                    matrix.iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
                } else {
                    matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                }
                lastTouchPos = touchPos;
                touchedLastFrame = true;
            } else if (mouseMode == MouseMode.HEAT) {
                if (touchedLastFrame) {
                    matrix.iterateAndHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                } else {
                    matrix.applyHeatByBrush(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
                }
                touchedLastFrame = true;
            }
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

    public void save(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                String lastClass;
                String currentClass;
                int currentClassCount;
                StringBuilder builder = new StringBuilder();
                for (int r = 0; r < matrix.outerArraySize; r++) {
                    Array<Element> row = matrix.getRow(r);
                    lastClass = row.get(0).getClass().getSimpleName();
                    currentClassCount = 0;
                    for (int e = 0; e < row.size; e++) {
                        Element element = row.get(e);
                        currentClass = element.getClass().getSimpleName();
                        if (currentClass.equals(lastClass)) {
                            currentClassCount++;
                            lastClass = currentClass;
                            if (e == row.size - 1) {
                                builder.append(currentClassCount);
                                builder.append(",");
                                builder.append(lastClass);
                                builder.append(",");
                            }
                            continue;
                        }
                        builder.append(currentClassCount);
                        builder.append(",");
                        builder.append(lastClass);
                        builder.append(",");
                        currentClassCount = 1;
                        lastClass = currentClass;
                    }
                    builder.append("|,");
                }
                out.write(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
