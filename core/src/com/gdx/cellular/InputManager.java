package com.gdx.cellular;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.input.InputProcessors;
import com.gdx.cellular.ui.CreatorMenu;
import com.gdx.cellular.util.TextInputHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InputManager {

    private final int maxBrushSize = 55;
    private final int minBrushSize = 3;
    private MouseMode mouseMode = MouseMode.SPAWN;

    private Vector3 mouseDownPos = new Vector3();

    private final int maxThreads = 50;

    private int brushSize = 5;

    private Vector3 lastTouchPos = new Vector3();
    private boolean touchedLastFrame = false;

    public ElementType currentlySelectedElement = ElementType.SAND;

    private boolean paused = false;
    private final TextInputHandler saveLevelNameListener = new TextInputHandler(this, this::setFileNameForSave);
    private final TextInputHandler loadLevelNameListener = new TextInputHandler(this, this::setFileNameForLoad);
    private final Path path = Paths.get("save/");
    private String fileNameForLevel;
    private boolean readyToSave = false;
    private boolean readyToLoad = false;
    public boolean drawMenu = false;

    public InputProcessor creatorInputProcessor;
    private CreatorMenu creatorMenu;

    public InputManager(Viewport viewport) {
        this.creatorMenu = new CreatorMenu(this, viewport);
    }

    public void setCurrentlySelectedElement(ElementType elementType) {
        this.currentlySelectedElement = elementType;
    }

    public MouseMode getMouseMode() {
        return this.mouseMode;
    }

    public void setCreatorInputProcessor(InputProcessor creatorInputProcessor) {
        this.creatorInputProcessor = creatorInputProcessor;
    }

    public void calculateNewBrushSize(int delta) {
        brushSize += delta;
        if (brushSize > maxBrushSize) brushSize = maxBrushSize;
        if (brushSize < minBrushSize) brushSize = minBrushSize;
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

    public void cycleMouseModes() {
        switch (mouseMode) {
            case SPAWN:
                this.mouseMode = MouseMode.HEAT;
                break;
            case HEAT:
                this.mouseMode = MouseMode.PARTICLE;
                break;
            case PARTICLE:
                this.mouseMode = MouseMode.PARTICALIZE;
                break;
            case PARTICALIZE:
                this.mouseMode = MouseMode.PHYSICSOBJ;
                break;
            case PHYSICSOBJ:
                this.mouseMode = MouseMode.RECTANGLE;
                break;
            case RECTANGLE:
                this.mouseMode = MouseMode.SPAWN;
        }
    }

    public void clearMatrix(CellularMatrix matrix) {
        matrix.clearAll();
    }

    public void placeSpout(CellularMatrix matrix, OrthographicCamera camera) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        if (mouseMode == MouseMode.SPAWN) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize, false);
        } else if (mouseMode == MouseMode.PARTICLE) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize, true);
        }
    }

    public void setTouchedLastFrame(boolean touchedLastFrame) {
        this.touchedLastFrame = touchedLastFrame;
    }

    public void spawnElementByInput(CellularMatrix matrix, OrthographicCamera camera) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (!touchedLastFrame) {
                mouseDownPos = touchPos.cpy();
            }
            switch (mouseMode) {
                case SPAWN:
                    if (touchedLastFrame) {
                        matrix.spawnElementBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
                    } else {
                        matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                case HEAT:
                    if (touchedLastFrame) {
                        matrix.applyHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                    } else {
                        CellularMatrix.FunctionInput input = new CellularMatrix.FunctionInput(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
                        matrix.applyHeatByBrush(input);
                    }
                    break;
                case PARTICLE:
                    if (touchedLastFrame) {
                        matrix.spawnParticleBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
                    } else {
                        matrix.spawnParticleByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                case PARTICALIZE:
                    if (touchedLastFrame) {
                        matrix.particalizeBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                    } else {
                        matrix.particalizeByPixelWithBrush((int) touchPos.x, (int) touchPos.y, brushSize);
                    }
                    break;
                case PHYSICSOBJ:
                    if (!touchedLastFrame) {
                        switch (currentlySelectedElement) {
                            case SAND:
                                ShapeFactory.createDefaultDynamicBox((int) touchPos.x, (int) touchPos.y, brushSize / 2);
                                break;
                            case STONE:
                                ShapeFactory.createDefaultDynamicCircle((int) touchPos.x, (int) touchPos.y, brushSize / 2);
                        }
                    }
                    break;
            }
            lastTouchPos = touchPos;
            touchedLastFrame = true;
//        } else {
//            boolean notTheSameLocation = lastTouchPos.x != mouseDownPos.x || lastTouchPos.y != mouseDownPos.y;
//            if (touchedLastFrame && mouseMode == MouseMode.RECTANGLE && notTheSameLocation) {
//                matrix.spawnRect(mouseDownPos, lastTouchPos, currentlySelectedElement);
//            }
//            touchedLastFrame = false;
    }

    public void openMenu() {

    }


    public boolean getIsPaused() {
        boolean stepOneFrame = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            stepOneFrame = true;
        }
        return paused && !stepOneFrame;
    }

    public void setIsPaused(boolean isPaused) {
        this.paused = isPaused;
    }

    public void togglePause() {
        paused = !paused;
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

    public void drawRect() {
    }

    public void drawMenu() {
        if (drawMenu) {
            this.creatorMenu.dropDownStage.act();
            this.creatorMenu.dropDownStage.draw();
        }
    }

    public void setDrawMenuAndLocation(float x, float y) {
        this.drawMenu = true;
        this.creatorMenu.dropDownTopLevelTable.setPosition(x, y);
        Gdx.input.setInputProcessor(this.creatorMenu.dropDownStage);
    }


    public void setMouseMode(MouseMode mode) {
        this.mouseMode = mode;
    }
}
