package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.EffectColors;

import java.util.BitSet;

public abstract class Element {

    private static final int REACTION_FRAME = 3;
    public static final int EFFECTS_FRAME = 1;
    public int pixelX;
    public int pixelY;

    public int matrixX;
    public int matrixY;
    public Vector3 vel;

    public float frictionFactor;
    public boolean isFreeFalling = true;
    public float inertialResistance;
    public int mass;
    public int health = 500;
    public int flammabilityResistance = 100;
    public int resetFlammabilityResistance = flammabilityResistance / 2;
    public boolean isIgnited;
    public int heatFactor = 10;
    public int fireDamage = 3;
    public boolean heated = false;
    public int temperature = 0;
    public int coolingFactor = 5;
    public Integer lifeSpan = null;
    public Color defaultColor;
    public ElementType elementType;

    public Color color;

    public BitSet stepped = new BitSet(1);

    public Element(int x, int y, boolean isPixel) {
        this.elementType = getEnumType();
        this.color = ColorConstants.getColorForElementType(this.elementType);
        if (isPixel) {
            setCoordinatesByPixel(x, y);
        } else {
            setCoordinatesByMatrix(x, y);
        }
        stepped.set(0, CellularAutomaton.stepped.get(0));
    }

//    public abstract void draw(ShapeRenderer sr);

    public void setVelocity(Vector3 vel) {
        this.vel = vel;
    }

    public abstract void step(CellularMatrix matrix);

    public boolean actOnOther(Element other, CellularMatrix matrix) {
        return false;
    }

    protected abstract boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth);

    public void swapPositions(CellularMatrix matrix, Element toSwap) {
        int toSwapMatrixX = toSwap.matrixX;
        int toSwapMatrixY = toSwap.matrixY;
        matrix.setElementAtIndex(this.matrixX, this.matrixY, toSwap);
        matrix.setElementAtIndex(toSwapMatrixX, toSwapMatrixY, this);
    }

    public void moveToLastValid(CellularMatrix matrix, Vector3 moveToLocation) {
        Element toSwap = matrix.get(moveToLocation.x, moveToLocation.y);
        swapPositions(matrix, toSwap);
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation) {
        moveToLastValidDieAndReplace(matrix, moveToLocation, this.elementType);
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation, ElementType elementType) {
        matrix.setElementAtIndex((int) moveToLocation.x, (int) moveToLocation.y, elementType.createElementByMatrix((int) moveToLocation.x, (int) moveToLocation.y));
        die(matrix);
    }

    public void moveToLastValidAndSwap(CellularMatrix matrix, Element toSwap, Vector3 moveToLocation) {
        int moveToLocationMatrixX = (int) moveToLocation.x;
        int moveToLocationMatrixY = (int) moveToLocation.y;
        Element thirdNeighbor = matrix.get(moveToLocationMatrixX, moveToLocationMatrixY);

        matrix.setElementAtIndex(this.matrixX, this.matrixY, thirdNeighbor);
        matrix.setElementAtIndex(toSwap.matrixX, toSwap.matrixY, this);
        matrix.setElementAtIndex(moveToLocationMatrixX, moveToLocationMatrixY, toSwap);
    }

    public void setCoordinatesByMatrix(int providedX, int providedY) {
        setXByMatrix(providedX);
        setYByMatrix(providedY);
    }

    public void setCoordinatesByPixel(int providedX, int providedY) {
        setXByPixel(providedX);
        setYByPixel(providedY);
    }

    public void setXByPixel(int providedVal) {
        this.pixelX = providedVal;
        this.matrixX = toMatrix(providedVal);
    }

    public void setYByPixel(int providedVal) {
        this.pixelY = providedVal;
        this.matrixY = toMatrix(providedVal);
    }

    public void setXByMatrix(int providedVal) {
        this.matrixX = providedVal;
        this.pixelX = toPixel(providedVal);
    }

    public void setYByMatrix(int providedVal) {
        this.matrixY = providedVal;
        this.pixelY = toPixel(providedVal);
    }

    private int toMatrix(int pixelVal) {
        return (int) Math.floor(pixelVal / CellularAutomaton.pixelSizeModifier);
    }

    private int toPixel(int pixelVal) {
        return (int) Math.floor(pixelVal * CellularAutomaton.pixelSizeModifier);
    }

    public boolean isReactionFrame() {
        return CellularAutomaton.frameCount == REACTION_FRAME;
    }

    public boolean isEffectsFrame() {
        return CellularAutomaton.frameCount == EFFECTS_FRAME;
    }

    public boolean corrode(CellularMatrix matrix) {
        this.health -= 170;
        checkIfDead(matrix);
        return true;
    }

    public boolean applyHeatToNeighborsIfIgnited(CellularMatrix matrix) {
        if (!isEffectsFrame() || !shouldApplyHeat()) return false;
        for (int x = matrixX - 1; x <= matrixX + 1; x++) {
            for (int y = matrixY - 1; y <= matrixY + 1; y++) {
                if (!(x == 0 && y == 0)) {
                    Element neighbor = matrix.get(x, y);
                    if (neighbor != null) {
                        neighbor.receiveHeat(heatFactor);
                    }
                }
            }
        }
        return true;
    }

    public boolean shouldApplyHeat() {
        return isIgnited || heated;
    }

    public boolean receiveHeat(int heat) {
        if (isIgnited) {
            return false;
        }
        this.flammabilityResistance -= (int) (Math.random() * heat);
        checkIfIgnited();
        return true;
    }

    public boolean receiveCooling(CellularMatrix matrix, int cooling) {
        if (isIgnited) {
            this.flammabilityResistance += cooling;
            checkIfIgnited();
            return true;
        }
        return false;
    }

    private void checkIfIgnited() {
        if (this.flammabilityResistance <= 0) {
            this.isIgnited = true;
            modifyColor();
        } else {
            this.isIgnited = false;
            this.color = ColorConstants.getColorForElementType(elementType);
        }
    }

    public void checkIfDead(CellularMatrix matrix) {
        if (this.health <= 0) {
            die(matrix);
        }
    }

    public void die(CellularMatrix matrix) {
        die(matrix, ElementType.EMPTYCELL);
    }

    protected void die(CellularMatrix matrix, ElementType type) {
        matrix.setElementAtIndex(matrixX, matrixY, type.createElementByMatrix(matrixX, matrixY));
        matrix.reportToChunkActive(matrixX, matrixY);
    }

    public void dieAndReplace(CellularMatrix matrix, ElementType type) {
        die(matrix, type);
    }

    public void takeEffectsDamage(CellularMatrix matrix) {
        if (!isEffectsFrame()) {
            return;
        }
        if (isIgnited) {
            health -= fireDamage;
            if (isSurrounded(matrix)) {
                flammabilityResistance = resetFlammabilityResistance;
            }
            checkIfIgnited();
        }
        checkIfDead(matrix);
    }

    private boolean isSurrounded(CellularMatrix matrix) {
        if (matrix.get(this.matrixX, this.matrixY + 1) instanceof EmptyCell) {
            return false;
        } else if (matrix.get(this.matrixX + 1, this.matrixY) instanceof EmptyCell) {
            return false;
        } else if (matrix.get(this.matrixX - 1, this.matrixY) instanceof EmptyCell) {
            return false;
        } else if (matrix.get(this.matrixX, this.matrixY - 1) instanceof EmptyCell) {
            return false;
        }
        return true;
    }

    public void spawnSparkIfIgnited(CellularMatrix matrix) {
        if (!isEffectsFrame() || !isIgnited) return;
        Element upNeighbor = matrix.get(matrixX, + matrixY + 1);
        if (upNeighbor != null) {
            if (upNeighbor instanceof EmptyCell) {
                ElementType elementToSpawn = Math.random() > .1 ? ElementType.SPARK : ElementType.SMOKE;
//                ElementType elementToSpawn = ElementType.SPARK;
                matrix.spawnElementByMatrix(matrixX, matrixY + 1, elementToSpawn);
            }
        }
    }

    public void modifyColor() {
        if (isIgnited) {
            color = EffectColors.getRandomFireColor();
        }
    }

    public void checkLifeSpan(CellularMatrix matrix) {
        if (lifeSpan != null) {
            lifeSpan--;
            if (lifeSpan <= 0) {
                die(matrix);
            }
        }
    }

    public int getRandomInt(int limit) {
        return (int) (Math.random() * limit);
    }

    public ElementType getEnumType() {
        return ElementType.valueOf(this.getClass().getSimpleName().toUpperCase());
    }
}
