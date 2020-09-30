package com.gdx.cellular.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.player.PlayerMeat;
import com.gdx.cellular.elements.solid.movable.MovableSolid;
import com.gdx.cellular.util.Assets;
import com.gdx.cellular.util.MaterialMap;

public class Player {

    Array<Array<Element>> bodyMeat;
    MaterialMap playerTexture;
    Vector3 vel;
    private int matrixX;
    private int matrixY;
    private float yThreshold = 0;
    private float xThreshold = 0;

    public Player(int x, int y, int playerIndex, CellularMatrix matrix) {
        this.bodyMeat = createBody(x, y, playerIndex, matrix);
        this.vel = new Vector3(0,-124,0);
        this.matrixX = x;
        this.matrixY = y;
    }

    public void addVelocity(Vector3 velToAdd) {
        this.vel.add(velToAdd);
    }

    public void addVelocity(float x, float y) {
        this.vel.x += x;
        this.vel.y += y;
    }

    public void setVelocity(Vector3 velToTake) {
        this.vel.x = velToTake.x;
        this.vel.y = velToTake.y;
    }

    public void setVelocity(float x, float y) {
        this.vel.x = x;
        this.vel.y = y;
    }

    public void setXVelocity(int x) {
        this.vel.x = x;
    }

    public void setYVelocity(int y) {
        this.vel.y = y;
    }

    public void step(CellularMatrix matrix) {
        addVelocity(CellularAutomaton.gravity);

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        float velYDeltaTimeFloat = (Math.abs(vel.y) * 1/60);
        float velXDeltaTimeFloat = (Math.abs(vel.x) * 1/60);
        int velXDeltaTime;
        int velYDeltaTime;
        if (velXDeltaTimeFloat < 1) {
            xThreshold += velXDeltaTimeFloat;
            velXDeltaTime = (int) xThreshold;
            if (Math.abs(velXDeltaTime) > 0) {
                xThreshold = 0;
            }
        } else {
            xThreshold = 0;
            velXDeltaTime = (int) velXDeltaTimeFloat;
        }
        if (velYDeltaTimeFloat < 1) {
            yThreshold += velYDeltaTimeFloat;
            velYDeltaTime = (int) yThreshold;
            if (Math.abs(velYDeltaTime) > 0) {
                yThreshold = 0;
            }
        } else {
            yThreshold = 0;
            velYDeltaTime = (int) velYDeltaTimeFloat;
        }

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        float floatFreq = (min == 0 || upperBound == 0) ? 0 : ((float) min / upperBound);
        int freqThreshold = 0;
        float freqCounter = 0;

        int smallerCount = 0;
        Vector3 formerLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        for (int i = 1; i <= upperBound; i++) {
            freqCounter += floatFreq;
            boolean thresholdPassed = Math.floor(freqCounter) > freqThreshold;
            if (floatFreq != 0 && thresholdPassed && min >= smallerCount) {
                freqThreshold = (int) Math.floor(freqCounter);
                smallerCount += 1;
            }

            int yIncrease, xIncrease;
            if (xDiffIsLarger) {
                xIncrease = i;
                yIncrease = smallerCount;
            } else {
                yIncrease = i;
                xIncrease = smallerCount;
            }

            int xOffset = xIncrease * xModifier;
            int yOffset = yIncrease * yModifier;
            boolean unstopped;
            for (Array<Element> meatRow : this.bodyMeat) {
                for (Element meat : meatRow) {
                    PlayerMeat playerMeat = (PlayerMeat) meat;
                    unstopped = playerMeat.stepAsPlayer(matrix, xOffset, yOffset);
                    if (!unstopped) {
                        moveToLastValid(matrix, lastValidLocation);
//                        this.vel.x = 0;
                        this.vel.y = 0;
                        return;
                    }
                }
            }
            lastValidLocation.x = getMatrixX() + xOffset;
            lastValidLocation.y = getMatrixY() + yOffset;
        }
        moveToLastValid(matrix, lastValidLocation);
    }

    private void moveToLastValid(CellularMatrix matrix, Vector3 lastValidLocation) {
        if (getMatrixX() == (int) lastValidLocation.x && getMatrixY() == (int) lastValidLocation.y) {
            return;
        }
        for (Array<Element> meatRow : this.bodyMeat) {
            for (Element meat : meatRow) {
                matrix.setElementAtIndex(meat.getMatrixX(), meat.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(0,0));
            }
        }
        int xOffset = (int) lastValidLocation.x - getMatrixX();
        int yOffset = (int) lastValidLocation.y - getMatrixY();
        for (Array<Element> meatRow : this.bodyMeat) {
            for (Element meat : meatRow) {
                PlayerMeat playerMeat = (PlayerMeat) meat;
                int neighborX = playerMeat.getMatrixX() + xOffset;
                int neighborY = playerMeat.getMatrixY() + yOffset;
                if (matrix.isWithinBounds(neighborX, neighborY)) {
                    Element neighbor = matrix.get(neighborX, neighborY);
                    if (neighbor instanceof EmptyCell) {
//                        playerMeat.swapPositions(matrix, neighbor, neighborX, neighborY);
                        matrix.setElementAtIndex(neighborX, neighborY, playerMeat);
                    } else if (neighbor instanceof Liquid || neighbor instanceof MovableSolid || neighbor instanceof Gas) {
                        ElementType.createParticleByMatrix(matrix, neighborX, neighborY, CellularAutomaton.gravity.cpy().scl(-1), neighbor);
                        matrix.setElementAtIndex(neighborX, neighborY, playerMeat);
                    } else if (neighbor instanceof PlayerMeat) {
                        playerMeat.moveToLocation(matrix, neighborX, neighborY);
                    }
                } else {
                    matrix.setElementAtIndex(playerMeat.getMatrixX(), playerMeat.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(playerMeat.getMatrixX(), playerMeat.getMatrixY()));
                }
            }
        }
        this.setMatrixX((int) lastValidLocation.x);
        this.setMatrixY((int) lastValidLocation.y);
    }

    private Array<Array<Element>> createBody(int worldX, int worldY, int playerIndex, CellularMatrix matrix) {
        Pixmap pixmap = Assets.getPixmap("elementtextures/Player0.png");
        this.playerTexture = new MaterialMap(pixmap);
        Array<Array<Element>> elements = new Array<>();
        for (int y = 0; y < playerTexture.h; y++) {
            Array<Element> innerArray = new Array<>();
            elements.add(innerArray);
            for (int x = 0; x < playerTexture.w; x++) {
                Element meat = ElementType.PLAYERMEAT.createElementByMatrix(worldX + x, worldY + y);
                matrix.setElementAtIndex(worldX + x, worldY + y, meat);
                ((PlayerMeat) meat).setOwningPlayer(this);
                int rgb = this.playerTexture.getRGB(x, y);
                Color color = new Color();
                Color.rgba8888ToColor(color, rgb);
                meat.color = color;
                innerArray.add(meat);
            }
        }
        return elements;
    }


    public void delete(CellularMatrix matrix) {
        bodyMeat.forEach(arr -> arr.forEach(meat -> matrix.setElementAtIndex(meat.getMatrixX(), meat.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(meat.getMatrixX(), meat.getMatrixY()))));
    }

    public int getMatrixX() {
        return matrixX;
    }

    public void setMatrixX(int matrixX) {
        this.matrixX = matrixX;
    }

    public int getMatrixY() {
        return matrixY;
    }

    public void setMatrixY(int matrixY) {
        this.matrixY = matrixY;
    }


}
