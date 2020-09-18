package com.gdx.cellular.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;

import static com.gdx.cellular.input.MouseMode.*;

public class Cursor {

    public InputManager inputManager;

    public Cursor(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void draw(ShapeRenderer sr) {
        MouseMode mode = inputManager.getMouseMode();
        Vector3 touchPos = inputManager.getTouchPos();
        int pixelX = (int) touchPos.x;
        int pixelY = (int) touchPos.y;
        int brushSize = inputManager.brushSize;
        InputManager.BRUSHTYPE brushtype = inputManager.brushType;
        switch(mode) {
            case EXPLOSION:
                sr.begin();
                sr.set(ShapeRenderer.ShapeType.Line);
                sr.setColor(Color.RED);
                sr.circle(pixelX, pixelY, brushSize - 2);
                sr.end();
                break;
            case SPAWN:
            case HEAT:
            case PARTICALIZE:
            case PARTICLE:
                if (brushtype == InputManager.BRUSHTYPE.CIRCLE) {
                    sr.begin();
                    sr.set(ShapeRenderer.ShapeType.Line);
                    sr.setColor(Color.RED);
                    sr.circle(pixelX, pixelY, brushSize - 2);
                    sr.end();
                } else if (brushtype == InputManager.BRUSHTYPE.SQUARE) {
                    sr.begin();
                    sr.set(ShapeRenderer.ShapeType.Line);
                    sr.setColor(Color.RED);
                    sr.rect(pixelX - brushSize, pixelY - brushSize, brushSize*2, brushSize*2);
                    sr.end();
                } else if (brushtype == InputManager.BRUSHTYPE.RECTANGLE) {
                    if (inputManager.touchedLastFrame) {
                        int width = (int) Math.abs(inputManager.rectStartPos.x - touchPos.x);
                        int height = (int) Math.abs(inputManager.rectStartPos.y - touchPos.y);
                        int xOrigin = (int) Math.min(inputManager.rectStartPos.x, touchPos.x);
                        int yOrigin = (int) Math.min(inputManager.rectStartPos.y, touchPos.y);
                        sr.begin();
                        sr.set(ShapeRenderer.ShapeType.Line);
                        sr.setColor(Color.RED);
                        sr.rect(xOrigin, yOrigin,  width, height);
                        sr.end();
                    }
                }
                break;
            case RECTANGLE:
                if (inputManager.touchedLastFrame) {
                    int width = (int) Math.abs(inputManager.rectStartPos.x - touchPos.x);
                    int height = (int) Math.abs(inputManager.rectStartPos.y - touchPos.y);
                    int xOrigin = (int) Math.min(inputManager.rectStartPos.x, touchPos.x);
                    int yOrigin = (int) Math.min(inputManager.rectStartPos.y, touchPos.y);
                    sr.begin();
                    sr.set(ShapeRenderer.ShapeType.Line);
                    sr.setColor(Color.RED);
                    sr.rect(xOrigin, yOrigin, width, height);
                    sr.end();
                }
                break;
            default:
        }

    }

}
