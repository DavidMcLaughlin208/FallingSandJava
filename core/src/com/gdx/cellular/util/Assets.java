package com.gdx.cellular.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

    private static final AssetManager assetManager;

    private Assets() {
        throw new IllegalStateException("Should not be instantiated");
    }

    static {
        assetManager = new AssetManager();
        FileHandle texturesFolder = new FileHandle("elementtextures");
        for (FileHandle fileHandle : texturesFolder.list()) {
            assetManager.load(texturesFolder.name() + "/" + fileHandle.name(), Pixmap.class);
        }
        assetManager.finishLoading();
    }

    public static Pixmap getPixmap(String s) {
        if (assetManager.isLoaded(s)) {
            return assetManager.get(s);
        } else {
            return null;
        }
    }

    public static Skin getSkin(String s) {
        if (assetManager.isLoaded(s)) {
            return assetManager.get(s);
        } else {
            return null;
        }
    }

}
