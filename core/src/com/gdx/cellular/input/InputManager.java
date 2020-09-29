package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.box2d.PhysicsElementActor;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.ui.CreatorMenu;
import com.gdx.cellular.ui.CursorActor;
import com.gdx.cellular.ui.ModeActor;
import com.gdx.cellular.util.TextInputHandler;
import com.gdx.cellular.util.WeatherSystem;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InputManager {

    private final int maxBrushSize = 205;
    private final int minBrushSize = 3;
    private MouseMode mouseMode = MouseMode.SPAWN;

    private final int maxThreads = 50;

    public int brushSize = 5;
    public BRUSHTYPE brushType = BRUSHTYPE.CIRCLE;

    private Vector3 lastTouchPos = new Vector3();
    public boolean touchedLastFrame = false;

    public ElementType currentlySelectedElement = ElementType.SAND;
    public BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;

    private boolean paused = false;
    private final TextInputHandler saveLevelNameListener = new TextInputHandler(this, this::setFileNameForSave);
    private final TextInputHandler loadLevelNameListener = new TextInputHandler(this, this::setFileNameForLoad);
    private final Path savePath = Paths.get("save/");
    private String fileNameForLevel;
    private boolean readyToSave = false;
    private boolean readyToLoad = false;
    public boolean drawMenu = false;
    private boolean drawCursor = true;

    public boolean earClip = false;

    public InputProcessor creatorInputProcessor;
    private final CreatorMenu creatorMenu;
    public Stage cursorStage;
    public Cursor cursor;
    public Stage modeStage;
    public Camera camera;
    public WeatherSystem weatherSystem;


    public Vector3 rectStartPos = new Vector3();

    public InputManager(OrthographicCamera camera, Viewport viewport, ShapeRenderer shapeRenderer) {
        this.camera = camera;
        this.creatorMenu = new CreatorMenu(this, viewport);
        this.cursorStage = new Stage(viewport);
        this.cursor = new Cursor(this);
        this.cursorStage.addActor(new CursorActor(shapeRenderer, this.cursor));
        this.modeStage = new Stage();
        this.modeStage.addActor(new ModeActor(this,0, CellularAutomaton.screenHeight - 23));
        this.weatherSystem = new WeatherSystem(ElementType.GUNPOWDER, 2);
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

    public void toggleEarClip() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            earClip = !earClip;
        }
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

    public void placeSpout(CellularMatrix matrix) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        if (mouseMode == MouseMode.SPAWN) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize, brushType, false);
        } else if (mouseMode == MouseMode.PARTICLE) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize, brushType, true);
        }
    }

    public void setTouchedLastFrame(boolean touchedLastFrame) {
        this.touchedLastFrame = touchedLastFrame;
    }

    public void spawnElementByInput(CellularMatrix matrix) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            switch (mouseMode) {
                case SPAWN:
                    switch (brushType) {
                        case SQUARE:
                        case CIRCLE:
                            if (touchedLastFrame) {
                                matrix.spawnElementBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize, brushType);
                            } else {
                                matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize, brushType);
                            }
                            break;
                        case RECTANGLE:
                            if (!touchedLastFrame) {
                                rectStartPos = new Vector3((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
                            }
                            break;
                    }
                    break;
                case EXPLOSION:
                    if (touchedLastFrame) {
                        return;
                    } else {
                        matrix.addExplosion(brushSize, 3, matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y));
                    }
                case HEAT:
                    if (touchedLastFrame) {
                        matrix.applyHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize, brushType);
                    } else {
                        CellularMatrix.FunctionInput input = new CellularMatrix.FunctionInput(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize, brushType);
                        matrix.applyHeatByBrush(input);
                    }
                    break;
                case PARTICLE:
                    if (touchedLastFrame) {
                        matrix.spawnParticleBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize, brushType);
                    } else {
                        matrix.spawnParticleByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize, brushType);
                    }
                    break;
                case PARTICALIZE:
                    if (touchedLastFrame) {
                        matrix.particalizeBetweenTwoPoints(lastTouchPos, touchPos, brushSize, brushType);
                    } else {
                        matrix.particalizeByPixelWithBrush((int) touchPos.x, (int) touchPos.y, brushSize, brushType);
                    }
                    break;
                case PHYSICSOBJ:
                    if (!touchedLastFrame) {
                        switch (currentlySelectedElement) {
                            case SAND:
                                spawnPhysicsBox((int) touchPos.x, (int) touchPos.y, brushSize, matrix);
                                break;
                            case STONE:
                                ShapeFactory.createDefaultDynamicCircle((int) touchPos.x, (int) touchPos.y, brushSize / 2);
                                break;
                            case DIRT:
                                spawnRandomPolygon((int) touchPos.x, (int) touchPos.y, getRandomPolygonArray(), matrix);

                        }
                    }
                    break;
                case RECTANGLE:
                    if (!touchedLastFrame) {
                        rectStartPos = new Vector3((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
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

    public void touchUpLMB(CellularMatrix matrix) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        switch (mouseMode) {
            case RECTANGLE:
                spawnPhysicsRect(matrix, touchPos);
                break;
            case SPAWN:
                switch (brushType) {
                    case RECTANGLE:
                        spawnRectangle(matrix, touchPos);
                        break;
                }
                break;
        }
    }

    private void spawnRectangle(CellularMatrix matrix, Vector3 touchPos) {
        int matrixX1 = matrix.toMatrix(touchPos.x);
        int matrixY1 = matrix.toMatrix(touchPos.y);
        int matrixX2 = matrix.toMatrix(rectStartPos.x);
        int matrixY2 = matrix.toMatrix(rectStartPos.y);
        int xStart = Math.min(matrixX1, matrixX2);
        int xEnd =  Math.max(matrixX1, matrixX2);
        int yStart = Math.min(matrixY1, matrixY2);
        int yEnd =  Math.max(matrixY1, matrixY2);

        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y < yEnd; y++) {
                matrix.spawnElementByMatrix(x, y, this.currentlySelectedElement);
            }
        }
    }

    private void spawnRandomPolygon(int x, int y, Array<Array<Element>> randomPolygonArray, CellularMatrix matrix) {
        Body body = ShapeFactory.createDynamicPolygonFromElementArray(matrix.toMatrix(x), matrix.toMatrix(y), randomPolygonArray);
        int mod = CellularAutomaton.box2dSizeModifier;
        Array<Fixture> fixtureList = body.getFixtureList();
        Vector2 point = new Vector2();
        int minX = matrix.innerArraySize;
        int maxY = 0;
        for (Fixture fixture : fixtureList) {
            PolygonShape shape = (PolygonShape) fixture.getShape();
            for (int i = 0; i < shape.getVertexCount(); i++) {
                shape.getVertex(i, point);
                Vector2 worldPoint = body.getWorldPoint(point);
                minX = Math.min(matrix.toMatrix(worldPoint.x * mod), minX);
                maxY = Math.max(matrix.toMatrix(worldPoint.y * mod), maxY);
            }
        }
        PhysicsElementActor physicsElementActor = new PhysicsElementActor(body, randomPolygonArray, minX, maxY);
        matrix.physicsElementActors.add(physicsElementActor);
    }

    public void spawnPhysicsBox(int x, int y, int brushSize, CellularMatrix matrix) {
        int matrixX = matrix.toMatrix(x);
        int matrixY = matrix.toMatrix(y);
        Body body =  ShapeFactory.createDefaultDynamicBox(x, y, brushSize / 2);
        PolygonShape shape = (PolygonShape) body.getFixtureList().get(0).getShape();
        Vector2 point = new Vector2();
        shape.getVertex(0, point);
        Vector2 worldPoint1 = body.getWorldPoint(point).cpy();
        shape.getVertex(2, point);
        Vector2 worldPoint2 = body.getWorldPoint(point).cpy();

//        Array<Array<Element>> elementList = new Array<>();
//        for (int xIndex = matrix.toMatrix((int) worldPoint1.x); xIndex < matrix.toMatrix((int) (worldPoint1.x + (worldPoint2.x - worldPoint1.x))); xIndex++) {
//            Array<Element> row = new Array<>();
//            elementList.add(row);
//            for (int yIndex = matrix.toMatrix((int) worldPoint2.y); yIndex > matrix.toMatrix((int) (worldPoint2.y + (worldPoint1.y - worldPoint2.y))); yIndex--) {
//                Element element = matrix.spawnElementByMatrix(matrix.toMatrix(x), matrix.toMatrix(y), ElementType.STONE);
//                row.add(element);
//            }
//        }

//        PhysicsElementActor physicsElementActor = new PhysicsElementActor(body, elementList);
//        matrix.physicsElementActors.add(physicsElementActor);

    }

    public void spawnPhysicsRect(CellularMatrix matrix, Vector3 touchPos) {
        touchPos.set((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
        spawnPhysicsRect(matrix, rectStartPos, lastTouchPos, currentlySelectedElement, bodyType);
    }

    public void spawnPhysicsRect(CellularMatrix matrix, Vector3 topLeft, Vector3 bottomRight, ElementType type, BodyDef.BodyType bodyType) {
        if (topLeft.x != bottomRight.x && topLeft.y != bottomRight.y) {
            matrix.spawnRect(topLeft, bottomRight, type, bodyType);
        }
    }

    private Array<Array<Element>> getRandomPolygonArray() {
        Array<Array<Element>> polygonElementArray = new Array<>();
        try {
            File folder = new File("customphysicsobjects");
            File[] listOfFiles = folder.listFiles();
            int index = (int) Math.floor(Math.random() * listOfFiles.length);
            File selectedFile = listOfFiles[index];
            Path filePath = Paths.get(selectedFile.toString());
            List<String> object = Files.readAllLines(filePath, StandardCharsets.UTF_8);
//            polygonElementArray.setSize(object.size());
            for (int r = object.size() -1; r >= 0; r--) {
                Array<Element> row = new Array<>();
                polygonElementArray.add(row);
                String line = object.get(r);
                String[] splitLine = line.split(",");
                for (int i = 0; i < splitLine.length; i++) {
                    String element = splitLine[i].trim().toUpperCase();
                    if (element.equals("NULL")) {
                        row.add(null);
                    } else {
                        row.add(ElementType.valueOf(element).createElementByMatrix(0, 0));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return polygonElementArray;
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
            Path newPath = savePath.resolve(fileNameForLevel + ".ser");
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
                matrix.clearAll();
                setIsPaused(false);
                Path newPath = savePath.resolve(fileNameForLevel + ".ser");
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

    public void drawMenu() {
        this.modeStage.draw();
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

    public Vector3 getTouchPos() {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        this.camera.unproject(touchPos);
        return touchPos;
    }

    public void drawCursor() {
        if (drawCursor) {
            cursorStage.draw();
        }
    }

    public void setCurrentElementOnWeather() {
        this.weatherSystem.setElementType(this.currentlySelectedElement);
    }

    public void setMouseMode(MouseMode mode) {
        this.mouseMode = mode;
    }

    public void setBodyType(BodyDef.BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public void clearBox2dActors() {
        ShapeFactory.clearAllActors();
    }

    public void cycleBrushType() {
        if (brushType == BRUSHTYPE.RECTANGLE) {
            brushType = BRUSHTYPE.SQUARE;
        } else if (brushType == BRUSHTYPE.SQUARE) {
            brushType = BRUSHTYPE.CIRCLE;
        } else if (brushType == BRUSHTYPE.CIRCLE) {
            brushType = BRUSHTYPE.RECTANGLE;
        }
    }

    public enum BRUSHTYPE {
        CIRCLE,
        SQUARE,
        RECTANGLE;
    }
}
