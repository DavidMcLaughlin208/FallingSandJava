package com.gdx.cellular.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.input.MouseMode;
import com.gdx.cellular.elements.ElementType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreatorMenu {
    private final int CELL_WIDTH = 120;
    private final int CELL_HEIGHT = 20;

    private final InputManager inputManager;
    public Table dropDownTopLevelTable;
    public Table dropDownElementList;
    public Table dropDownMouseMode;
    public Stage dropDownStage;
    public boolean showElementList = false;
    public SelectedSubList selectedSubList;

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
                super.setPosition(x, y);
                dropDownElementList.setPosition(this.getX() + CELL_WIDTH, this.getY());
                dropDownMouseMode.setPosition(this.getX() + CELL_WIDTH, this.getY() - CELL_HEIGHT);
            }

        };
        Button accessElementList = createAccessSublistButton(skin, "Elements", SelectedSubList.ELEMENT);
        dropDownTopLevelTable.add(accessElementList).width(CELL_WIDTH).height(CELL_HEIGHT);
        dropDownTopLevelTable.row();
        Button accessMouseModeList = createAccessSublistButton(skin, "Mouse Modes", SelectedSubList.MOUSEMODE);
        dropDownTopLevelTable.add(accessMouseModeList).width(CELL_WIDTH).height(CELL_HEIGHT);


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
//        dropDownTopLevelTable.debug();

        stage.addActor(dropDownTopLevelTable);
        stage.addActor(dropDownElementList);
        stage.addActor(dropDownMouseMode);

        dropDownStage = stage;
    }

    private Button createAccessSublistButton(Skin skin, String text, SelectedSubList subList) {
        Button button = new TextButton(text, skin);
        button.addListener(new ClickListener(){
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                selectedSubList = subList;
            }
            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {

            }
        });
        return button;
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

    private Skin createSkin(String name) {
        FileHandle atlasFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle(name + ".atlas", com.badlogic.gdx.Files.FileType.Internal)));
        FileHandle skinFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle(name + ".json", com.badlogic.gdx.Files.FileType.Internal)));
        FileHandle imagesFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle("", com.badlogic.gdx.Files.FileType.Internal)));
        return new Skin(skinFileHandler, new TextureAtlas(atlasFileHandler, imagesFileHandler));
    }

    private enum SelectedSubList {
        ELEMENT,
        MOUSEMODE
    }

}
