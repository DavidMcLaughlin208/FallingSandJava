package com.gdx.cellular;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.util.TextInputHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputManager {

    private final int maxBrushSize = 55;
    private final int minBrushSize = 3;
    private final int brushIncrements = 2;
    private MouseMode mouseMode = MouseMode.SPAWN;

    private final int maxThreads = 50;

    private Vector3 lastTouchPos = new Vector3();
    private boolean touchedLastFrame = false;

    private boolean paused = false;
    private final TextInputHandler saveLevelNameListener = new TextInputHandler(this, this::setFileNameForSave);
    private final TextInputHandler loadLevelNameListener = new TextInputHandler(this, this::setFileNameForLoad);
    private final Path path = Paths.get("save/");
    private String fileNameForLevel;
    private boolean readyToSave = false;
    private boolean readyToLoad = false;

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
            elementType = ElementType.EMPTYCELL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            elementType = ElementType.SPARK;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            elementType = ElementType.EMBER;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            elementType = ElementType.LAVA;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            elementType = ElementType.COAL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            elementType = ElementType.FLAMMABLEGAS;
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

    public boolean toggleChunks(boolean toggleChunks) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            return !toggleChunks;
        } else {
            return toggleChunks;
        }
    }

    public boolean cycleMouseModes() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (this.mouseMode == MouseMode.SPAWN) {
                this.mouseMode = MouseMode.HEAT;
            } else if (this.mouseMode == MouseMode.HEAT) {
                this.mouseMode = MouseMode.PARTICLE;
            } else if (this.mouseMode == MouseMode.PARTICLE) {
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
            if (mouseMode == MouseMode.SPAWN) {
                matrix.addSpout(currentlySelectedElement, touchPos, brushSize, false);
            } else if (mouseMode == MouseMode.PARTICLE) {
                matrix.addSpout(currentlySelectedElement, touchPos, brushSize, true);
            }
        }
    }

    public void spawnElementByInput(CellularMatrix matrix, OrthographicCamera camera, ElementType currentlySelectedElement, int brushSize, Vector3 velocity) {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (mouseMode == MouseMode.SPAWN) {
                if (touchedLastFrame) {
                    matrix.spawnElementBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
                } else {
                    matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                }
                lastTouchPos = touchPos;
                touchedLastFrame = true;
            } else if (mouseMode == MouseMode.HEAT) {
                if (touchedLastFrame) {
                    matrix.applyHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                } else {
                    CellularMatrix.FunctionInput input = new CellularMatrix.FunctionInput(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
                    matrix.applyHeatByBrush(input);
                }
                touchedLastFrame = true;
            } else if (mouseMode == MouseMode.PARTICLE) {
                if (touchedLastFrame) {
                    matrix.spawnParticleBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize, velocity);
                } else {
                    matrix.spawnParticleByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize, velocity);
                }
                lastTouchPos = touchPos;
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

    public void setIsPaused(boolean isPaused) {
        this.paused = isPaused;
    }

    public void save(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && !readyToSave) {
            paused = true;
            Gdx.input.getTextInput(saveLevelNameListener, "Save Level", "File Name", "");
        }
        if (readyToSave) {
            Path newPath = path.resolve(fileNameForLevel + ".ser");
            if (!Files.exists(newPath)) {
                try {
                    Files.createDirectories(newPath.getParent());
                    Files.createFile(newPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            readyToSave = false;
            setIsPaused(false);
            try (Writer out = Files.newBufferedWriter(newPath, StandardCharsets.UTF_8)) {
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
                    builder.append("0,|,");
                }
                out.write(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            paused = true;
            Gdx.input.getTextInput(loadLevelNameListener, "Load Level", "File Name", "");
        }
        if (readyToLoad) {
            try {
                readyToLoad = false;
                setIsPaused(false);
                Path newPath = path.resolve(fileNameForLevel + ".ser");
                String level = Files.readAllLines(newPath, StandardCharsets.UTF_8).get(0);
                String[] splitLevel = level.split(",");
                Array<Element> row = matrix.getRow(0);
                int lastElementIndex = 0;
                int rowIndex = 0;
                for (int i = 0; i < splitLevel.length; i += 2) {
                    int count = Integer.parseInt((String) java.lang.reflect.Array.get(splitLevel, i));
                    String clazz = ((String) java.lang.reflect.Array.get(splitLevel, i + 1)).toUpperCase();
                    if (clazz.equals("|")) {
                        rowIndex++;
                        lastElementIndex = 0;
                        if (rowIndex > matrix.outerArraySize - 1) {
                            continue;
                        }
                        row = matrix.getRow(rowIndex);
                        continue;
                    }
                    for (int k = 0; k < count; k++) {
                        row.set(k + lastElementIndex, ElementType.valueOf(clazz).createElementByMatrix(k + lastElementIndex, rowIndex));
                    }
                    lastElementIndex += count;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean setFileNameForSave(String sane) {
        this.fileNameForLevel = sane;
        this.readyToSave = true;
        return true;
    }

    public boolean setFileNameForLoad(String sane) {
        this.fileNameForLevel = sane;
        this.readyToLoad = true;
        return true;
    }

}
