package com.gdx.cellular.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;
import java.util.Map;

public class Skins {

    private static final Map<String, Skin> skinMap = new HashMap<>();
    private static final AssetManager assetManager = new AssetManager();

    private Skins() {
        throw new IllegalStateException("Should not be instantiated");
    }

    static public Skin getSkin(String skinName) {
        Skin skin = skinMap.get(skinName);
        if (skin == null) {
            assetManager.load(skinName + ".atlas", TextureAtlas.class);
            SkinLoader.SkinParameter parameter = new SkinLoader.SkinParameter(skinName + ".atlas");
            assetManager.load(skinName + ".json", Skin.class, parameter);
            assetManager.finishLoading();
            Skin loadedSkin = assetManager.get(skinName + ".json", Skin.class);
            skinMap.put(skinName, loadedSkin);
            skin = loadedSkin;
        }
        return skin;
    }


}
