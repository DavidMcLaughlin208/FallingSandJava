package com.gdx.cellular.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.input.MouseMode;
import com.gdx.cellular.elements.ElementType;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CreatorMenu {
    private final int CELL_WIDTH = 120;
    private final int CELL_HEIGHT = 20;

    private final InputManager inputManager;
    public Table dropDownTopLevelTable;
    public Table dropDownElementList;
    public Table dropDownMouseMode;
    public Table dropDownBodyType;
    public Table dropDownWeather;
    public Stage dropDownStage;
    public SelectedSubList selectedSubList;
    AssetManager manager = new AssetManager();

    public Map<SelectedSubList, Table> listTableMap = new HashMap<>();

    public CreatorMenu(InputManager inputManager, Viewport viewport) {
        this.inputManager = inputManager;
        createDropdownStage(viewport);
    }

    private void createDropdownStage(Viewport viewport) {
        Stage stage = new Stage(viewport);
        Skin skin = Skins.getSkin("uiskin");

        dropDownTopLevelTable = new Table() {
            @Override
            public void setPosition (float x, float y) {
                int dropDownListY = (int) y;
                if (CellularAutomaton.screenHeight - y < dropDownTopLevelTable.getRows() / 2f * CELL_HEIGHT) {
                    dropDownListY = (int) (CellularAutomaton.screenHeight - (dropDownTopLevelTable.getRows() / 2f * CELL_HEIGHT));
                } else if (y < dropDownTopLevelTable.getRows() / 2f * CELL_HEIGHT) {
                    dropDownListY = (int) (dropDownTopLevelTable.getRows() / 2f * CELL_HEIGHT);
                }
                int dropDownListX = (int) x;
                if (CellularAutomaton.screenWidth - x < CELL_WIDTH / 2f) {
                    dropDownListX = (int) (CellularAutomaton.screenWidth - CELL_WIDTH / 2f);
                } else if (x < CELL_WIDTH / 2f) {
                    dropDownListX = (int) (CELL_WIDTH / 2f);
                }
                super.setPosition(dropDownListX, dropDownListY);
                dropDownElementList.setPosition(-200, -200);
                dropDownMouseMode.setPosition(-200, -200);
                dropDownBodyType.setPosition(-200, -200);
                dropDownWeather.setPosition(-200, -200);
                unhideSelectedSublist(SelectedSubList.ELEMENT);
            }

        };
        Button accessElementList = createAccessSublistButton(skin, "Elements", SelectedSubList.ELEMENT);
        dropDownTopLevelTable.add(accessElementList).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownTopLevelTable.row();
        Button accessMouseModeList = createAccessSublistButton(skin, "Mouse Modes", SelectedSubList.MOUSEMODE);
        dropDownTopLevelTable.add(accessMouseModeList).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownTopLevelTable.row();
        Button weatherList = createAccessSublistButton(skin, "Weather", SelectedSubList.WEATHER);
        dropDownTopLevelTable.add(weatherList).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownTopLevelTable.row();
        Button bodyTypeList = createAccessSublistButton(skin, "Body Type", SelectedSubList.BODYTYPE);
        dropDownTopLevelTable.add(bodyTypeList).width(CELL_WIDTH).height(CELL_HEIGHT);


        // Element Sublist
        dropDownElementList = new Table() {
            @Override
            public void draw (Batch batch, float parentAlpha) {
                if (SelectedSubList.ELEMENT.equals(selectedSubList)) {
                    super.draw(batch, parentAlpha);
                }
            }
        };
        dropDownElementList.add(new Label("Solids", skin)).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownElementList.row();
        List<Button> immovableSolidsButtons = createElementButtons(ElementType.getSolids(), skin);
        immovableSolidsButtons.forEach(button -> {
            dropDownElementList.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownElementList.row();
        });
        dropDownElementList.add(new Label("Liquids", skin)).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownElementList.row();
        List<Button> liquidButtons = createElementButtons(ElementType.getLiquids(), skin);
        liquidButtons.forEach(button -> {
            dropDownElementList.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownElementList.row();
        });
        dropDownElementList.add(new Label("Gasses", skin)).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownElementList.row();
        List<Button> gasButtons = createElementButtons(ElementType.getGasses(), skin);
        gasButtons.forEach(button -> {
            dropDownElementList.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownElementList.row();
        });

        // Mouse Mode Sublist
        dropDownMouseMode = new Table() {
            @Override
            public void draw (Batch batch, float parentAlpha) {
                if (SelectedSubList.MOUSEMODE.equals(selectedSubList)) {
                    super.draw(batch, parentAlpha);
                }
            }
        };
        List<Button> mouseModeButtons = createMouseModeButtons(skin);
        mouseModeButtons.forEach(button -> {
            dropDownMouseMode.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownMouseMode.row();
        });

        // Body Type Sublist
        dropDownBodyType = new Table() {
            @Override
            public void draw (Batch batch, float parentAlpha) {
                if (SelectedSubList.BODYTYPE.equals(selectedSubList)) {
                    super.draw(batch, parentAlpha);
                }
            }
        };
        List<Button> bodyTypeButtons = createBodyTypeButtons(skin);
        bodyTypeButtons.forEach(button -> {
            dropDownBodyType.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownBodyType.row();
        });

        // Weather Sublist
        dropDownWeather = new Table() {
            @Override
            public void draw (Batch batch, float parentAlpha) {
                if (SelectedSubList.WEATHER.equals(selectedSubList)) {
                    super.draw(batch, parentAlpha);
                }
            }
        };
        List<Button> weatherButtons = createWeatherButtons(skin);
        weatherButtons.forEach(button -> {
            dropDownWeather.add(button).width(CELL_WIDTH).height(CELL_HEIGHT);
            dropDownWeather.row();
        });

//        dropDownTopLevelTable.debug();

        stage.addActor(dropDownTopLevelTable);
        stage.addActor(dropDownElementList);
        stage.addActor(dropDownMouseMode);
        stage.addActor(dropDownWeather);
        stage.addActor(dropDownBodyType);

        listTableMap.put(SelectedSubList.ELEMENT, dropDownElementList);
        listTableMap.put(SelectedSubList.MOUSEMODE, dropDownMouseMode);
        listTableMap.put(SelectedSubList.WEATHER, dropDownWeather);
        listTableMap.put(SelectedSubList.BODYTYPE, dropDownBodyType);

        dropDownStage = stage;
    }

    private Button createAccessSublistButton(Skin skin, String text, SelectedSubList subList) {
        Button button = new TextButton(text, skin);
        button.setColor(Color.GRAY);
        button.addListener(new ClickListener(){
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hideSelectedList(selectedSubList);
                selectedSubList = subList;
                unhideSelectedSublist(selectedSubList);
                button.setColor(Color.RED);
            }
            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setColor(Color.GRAY);
            }
        });
        return button;
    }

    private void unhideSelectedSublist(SelectedSubList selectedSubList) {
        Table list = getList(selectedSubList);
        if (list != null) {
            int dropDownListY = (int) dropDownTopLevelTable.getY();
            if (CellularAutomaton.screenHeight - dropDownTopLevelTable.getY() < list.getRows() / 2f * CELL_HEIGHT) {
                dropDownListY = (int) (CellularAutomaton.screenHeight - (list.getRows() / 2f * CELL_HEIGHT));
            } else if (dropDownTopLevelTable.getY() < list.getRows() / 2f * CELL_HEIGHT) {
                dropDownListY = (int) (list.getRows() / 2f * CELL_HEIGHT);
            }
            int dropDownListX = (int) dropDownTopLevelTable.getX() + CELL_WIDTH;
            if (CellularAutomaton.screenWidth - dropDownTopLevelTable.getX() < CELL_WIDTH * 1.5f) {
                dropDownListX = (int) (dropDownTopLevelTable.getX() - CELL_WIDTH);
            }
            list.setPosition(dropDownListX, dropDownListY);
        }
    }

    private void hideSelectedList(SelectedSubList selectedSubList) {
        Table list = getList(selectedSubList);
        if (list != null) {
            list.setPosition(-200, -200);
        }
    }

    private Table getList(SelectedSubList selectedSubList) {
        return listTableMap.get(selectedSubList);
    }

    private List<Button> createElementButtons(List<ElementType> elements, Skin skin) {
        return elements.stream().map(elementType -> createElementButton(skin, elementType)).collect(Collectors.toList());
    }

    private Button createElementButton(Skin skin, ElementType elementType) {
        Button button = new TextButton(elementType.toString(), skin);
        button.setColor(Color.GRAY);
        button.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.drawMenu = false;
                Gdx.input.setInputProcessor(inputManager.creatorInputProcessor);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.currentlySelectedElement = elementType;
                return true;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setColor(Color.GRAY);
            }
        });
        return button;
    }

    private List<Button> createMouseModeButtons(Skin skin) {
        return Arrays.stream(MouseMode.values()).map(mode -> createMouseModeButton(skin, mode)).collect(Collectors.toList());
    }

    private Button createMouseModeButton(Skin skin, MouseMode mode) {
        Button button = new TextButton(mode.toString(), skin);
        button.setColor(Color.GRAY);
        button.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.drawMenu = false;
                Gdx.input.setInputProcessor(inputManager.creatorInputProcessor);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.setMouseMode(mode);
                return true;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setColor(Color.GRAY);
            }
        });
        return button;
    }

    private List<Button> createBodyTypeButtons(Skin skin) {
        return Arrays.stream(BodyDef.BodyType.values()).map(mode -> createBodyTypeButton(skin, mode)).collect(Collectors.toList());
    }

    private Button createBodyTypeButton(Skin skin, BodyDef.BodyType bodyType) {
        Button button = new TextButton(bodyType.toString(), skin);
        button.setColor(Color.GRAY);
        button.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.drawMenu = false;
                Gdx.input.setInputProcessor(inputManager.creatorInputProcessor);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.setBodyType(bodyType);
                return true;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setColor(Color.GRAY);
            }
        });
        return button;
    }

    private List<Button> createWeatherButtons(Skin skin) {
        List<Button> buttons = new ArrayList<>();
        Button toggleWeatherButton = new TextButton("Toggle On/Off", skin);
        toggleWeatherButton.setColor(Color.GRAY);
        toggleWeatherButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.drawMenu = false;
                inputManager.weatherSystem.toggle();
                Gdx.input.setInputProcessor(inputManager.creatorInputProcessor);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                toggleWeatherButton.setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleWeatherButton.setColor(Color.GRAY);
            }
        });
        buttons.add(toggleWeatherButton);
        Button setElementButton = new TextButton("Set Element", skin);
        setElementButton.setColor(Color.GRAY);
        setElementButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.drawMenu = false;
                inputManager.setCurrentElementOnWeather();
                Gdx.input.setInputProcessor(inputManager.creatorInputProcessor);
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                setElementButton.setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                setElementButton.setColor(Color.GRAY);
            }
        });
        buttons.add(setElementButton);
        return buttons;
    }

    private enum SelectedSubList {
        ELEMENT,
        MOUSEMODE,
        BODYTYPE,
        WEATHER
    }

}
