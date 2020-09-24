package com.gdx.cellular.player;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.util.MaterialMap;

public class Player {

    Array<Array<Element>> bodyMeat;
    MaterialMap playerTexture;
    Vector3 vel;

    public Player(int x, int y, int playerIndex, CellularMatrix matrix) {
        this.bodyMeat = createBody(x, y, playerIndex, matrix);
        this.vel = new Vector3(0,0,0);
    }

    public void step(CellularMatrix matrix) {
        vel.add(CellularAutomaton.gravity);
    }

    private Array<Array<Element>> createBody(int worldX, int worldY, int playerIndex, CellularMatrix matrix) {
//        File textureFile = new File("elementtextures/Player" + playerIndex + ".png");
//        this.playerTexture = new MaterialMap(textureFile);
        Array<Array<Element>> elements = new Array<>();
//        for (int x = 0; x < playerTexture.w; x++) {
//            Array<Element> innerArray = new Array<>();
//            elements.add(innerArray);
//            for (int y = 0; y < playerTexture.h; y++) {
//                Element meat = ElementType.PLAYERMEAT.createElementByMatrix(worldX + x, worldY + y);
//                matrix.setElementAtIndex(worldX + x, worldY + y, meat);
//                int rgb = this.playerTexture.getRGB(x, y);
//                Color color = new Color();
//                Color.argb8888ToColor(color, rgb);
//                meat.color = color;
//                innerArray.add(meat);
//            }
//        }
        return elements;
    }


    public void delete(CellularMatrix matrix) {
        bodyMeat.forEach(arr -> arr.forEach(meat -> matrix.setElementAtIndex(meat.getMatrixX(), meat.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(meat.getMatrixX(), meat.getMatrixY()))));
    }
}
