package com.gdx.cellular.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MaterialMap {

    BufferedImage img;
    int w;
    int h;

    public MaterialMap(File file) {
        try {
            img = ImageIO.read(file);
            w = img.getWidth();
            h = img.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRGB(int x, int y) {
        int relativeX = x == 0 ? 0 : Math.abs(x) % w;
        int relativeY = y == 0 ? 0 : Math.abs(y) % h;
        return img.getRGB(relativeX, relativeY);
    }
}
