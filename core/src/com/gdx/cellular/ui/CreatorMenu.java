package com.gdx.cellular.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.input.MouseMode;
import com.gdx.cellular.elements.ElementType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreatorMenu {
    private final int CELL_WIDTH = 120;
    private final int CELL_HEIGHT = 20;

    private final InputManager inputManager;
    public Table dropDownTopLevelTable;
    public Table dropDownElementList;
    public Table dropDownMouseMode;
    public Table dropDownBodyType;
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
        Skin skin = createSkin("uiskin");



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
                dropDownElementList.setPosition(this.getX() + CELL_WIDTH, this.getY());
                dropDownMouseMode.setPosition(this.getX() + CELL_WIDTH, this.getY() - CELL_HEIGHT);
                dropDownBodyType.setPosition(this.getX() + CELL_WIDTH, this.getY() - CELL_HEIGHT * 2);
            }

        };
        Button accessElementList = createAccessSublistButton(skin, "Elements", SelectedSubList.ELEMENT);
        dropDownTopLevelTable.add(accessElementList).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownTopLevelTable.row();
        Button accessMouseModeList = createAccessSublistButton(skin, "Mouse Modes", SelectedSubList.MOUSEMODE);
        dropDownTopLevelTable.add(accessMouseModeList).width(CELL_WIDTH).height(CELL_HEIGHT);
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
        List<Button> elementButtons = createElementButtons(skin);
        elementButtons.forEach(button -> {
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

        // Body Type Sublist

//        dropDownTopLevelTable.debug();

        stage.addActor(dropDownTopLevelTable);
        stage.addActor(dropDownElementList);
        stage.addActor(dropDownMouseMode);
        stage.addActor(dropDownBodyType);

        listTableMap.put(SelectedSubList.ELEMENT, dropDownElementList);
        listTableMap.put(SelectedSubList.MOUSEMODE, dropDownMouseMode);
        listTableMap.put(SelectedSubList.BODYTYPE, dropDownBodyType);

        dropDownStage = stage;
    }

    private Button createAccessSublistButton(Skin skin, String text, SelectedSubList subList) {
        Button button = new TextButton(text, skin);
        button.addListener(new ClickListener(){
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hideSelectedList(selectedSubList);
                selectedSubList = subList;
                unhideSelectedSublist(selectedSubList);
            }
            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {

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
            list.setPosition(0, 0);
        }
    }

    private Table getList(SelectedSubList selectedSubList) {
        return listTableMap.get(selectedSubList);
    }

    private List<Button> createElementButtons(Skin skin) {
        return Arrays.stream(ElementType.values()).map(elementType -> createElementButton(skin, elementType)).collect(Collectors.toList());
    }

    private Button createElementButton(Skin skin, ElementType elementType) {
        Button button = new TextButton(elementType.toString(), skin);
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
        });
        return button;
    }

    private List<Button> createMouseModeButtons(Skin skin) {
        return Arrays.stream(MouseMode.values()).map(mode -> createMouseModeButton(skin, mode)).collect(Collectors.toList());
    }

    private Button createMouseModeButton(Skin skin, MouseMode mode) {
        Button button = new TextButton(mode.toString(), skin);
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
        });
        return button;
    }

    private List<Button> createBodyTypeButtons(Skin skin) {
        return Arrays.stream(BodyDef.BodyType.values()).map(mode -> createBodyTypeButton(skin, mode)).collect(Collectors.toList());
    }

    private Button createBodyTypeButton(Skin skin, BodyDef.BodyType bodyType) {
        Button button = new TextButton(bodyType.toString(), skin);
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
        });
        return button;
    }

    private Skin createSkin(String name) {
        manager.load(name + ".atlas", TextureAtlas.class);
        SkinLoader.SkinParameter parameter = new SkinLoader.SkinParameter(name + ".atlas");
        manager.load(name + ".json", Skin.class, parameter);
        manager.finishLoading();
        return manager.get(name + ".json", Skin.class);
    }

    private enum SelectedSubList {
        ELEMENT,
        MOUSEMODE,
        BODYTYPE
    }

}
