package com.gdx.cellular.util;

import com.badlogic.gdx.graphics.Pixmap;

public class MaterialMap {

    Pixmap img;
    public int w;
    public int h;

    public MaterialMap(Pixmap img) {
        this.img = img;
        this.w = img.getWidth();
        this.h = img.getHeight();
    }

    public int getRGB(int x, int y) {
        int relativeX = x == 0 ? 0 : Math.abs(x) % w;
        int relativeY = y == 0 ? 0 : Math.abs(y) % h;
        return img.getPixel(relativeX, relativeY);
    }
}
