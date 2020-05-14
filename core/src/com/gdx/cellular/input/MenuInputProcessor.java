package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.InputManager;

public class MenuInputProcessor implements InputProcessor {

    private Stage dropDownStage;
    private Table dropDownTopLevelTable;

    private boolean showMenu = false;

    private final InputManager inputManager;
    private final OrthographicCamera camera;
    private final CellularMatrix matrix;


    public MenuInputProcessor(InputManager inputManager, OrthographicCamera camera, CellularMatrix matrix) {
        super();
        createDropdownStage();
        this.inputManager = inputManager;
        this.camera = camera;
        this.matrix = matrix;
    }

    private void createDropdownStage() {
        Stage stage = new Stage(new ScreenViewport());

        FileHandle atlasFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle("uiskin.atlas", com.badlogic.gdx.Files.FileType.Internal)));
        FileHandle skinFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle("uiskin.json", com.badlogic.gdx.Files.FileType.Internal)));
        FileHandle imagesFileHandler = new FileHandle(String.valueOf(Gdx.files.getFileHandle("", com.badlogic.gdx.Files.FileType.Internal)));
        Skin skin = new Skin(skinFileHandler, new TextureAtlas(atlasFileHandler, imagesFileHandler));
        Label nameLabel = new Label("Name:", skin);
        TextField nameText = new TextField("", skin);
        Label addressLabel = new Label("Address:", skin);
        TextField addressText = new TextField("", skin);

        dropDownTopLevelTable = new Table();
        dropDownTopLevelTable.add(nameLabel);
        dropDownTopLevelTable.add(nameText).width(100);
        dropDownTopLevelTable.row();
        dropDownTopLevelTable.add(addressLabel);
        dropDownTopLevelTable.add(addressText).width(100);
        dropDownTopLevelTable.debug();

        stage.addActor(dropDownTopLevelTable);


        dropDownStage = stage;
    }

    @Override
    public boolean keyDown(int keycode) {
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
        if (button == Input.Buttons.RIGHT) {
            showMenu = !showMenu;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
