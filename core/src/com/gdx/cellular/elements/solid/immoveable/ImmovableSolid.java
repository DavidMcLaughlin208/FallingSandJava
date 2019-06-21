package com.gdx.cellular.elements.solid.immoveable;

import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.solid.Solid;

public abstract class ImmovableSolid extends Solid {

    public ImmovableSolid(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
    }

//    @Override
//    public void draw(ShapeRenderer sr) {
//        sr.setColor(color);
//        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
//    }

    @Override
    public void step(CellularMatrix matrix) {
        applyHeatToNeighborsIfIgnited(matrix);
        takeEffectsDamage(matrix);
        spawnSparkIfIgnited(matrix);
        modifyColor();
    }
}
