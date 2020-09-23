package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.box2d.PhysicsElementActor;
import com.gdx.cellular.effects.EffectColors;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class Element {

    private static final int REACTION_FRAME = 3;
    public static final int EFFECTS_FRAME = 1;
    public int pixelX;
    public int pixelY;

    public int matrixX;
    public int matrixY;
    public Vector3 vel;

    public List<Vector2> secondaryMatrixCoords = new ArrayList<>();

    public float frictionFactor;
    public boolean isFreeFalling = true;
    public float inertialResistance;
    public int stoppedMovingCount = 0;
    public int stoppedMovingThreshold = 1;
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
    public ElementType elementType;
    public PhysicsElementActor owningBody = null;
    public Vector2 owningBodyCoords = null;
    public int explosionResistance = 1;
    public int explosionRadius = 0;
    public boolean discolored = false;

    public float xThreshold = 0;
    public float yThreshold = 0;

    public boolean isDead = false;

    public Color color;

    public BitSet stepped = new BitSet(1);

    public Element(int x, int y) {
        this.elementType = getEnumType();
        setCoordinatesByMatrix(x, y);
        this.color = ColorConstants.getColorForElementType(this.elementType, x, y);
        stepped.set(0, CellularAutomaton.stepped.get(0));
    }

//    public abstract void draw(ShapeRenderer sr);

    public void customElementFunctions(CellularMatrix matrix) { }

    public void setVelocity(Vector3 vel) {
        this.vel = vel;
    }

    public abstract void step(CellularMatrix matrix);

    public boolean actOnOther(Element other, CellularMatrix matrix) {
        return false;
    }

    protected abstract boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth);

    public void swapPositions(CellularMatrix matrix, Element toSwap) {
        swapPositions(matrix, toSwap, toSwap.matrixX, toSwap.matrixY);
    }

    public void swapPositions(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY) {
        matrix.setElementAtIndex(this.matrixX, this.matrixY, toSwap);
        matrix.setElementAtIndex(toSwapX, toSwapY, this);
    }

    public void moveToLastValid(CellularMatrix matrix, Vector3 moveToLocation) {
        if ((int) (moveToLocation.x) == matrixX && (int) (moveToLocation.y) == matrixY) return;
        Element toSwap = matrix.get(moveToLocation.x, moveToLocation.y);
        swapPositions(matrix, toSwap, (int) moveToLocation.x, (int) moveToLocation.y);
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation) {
        moveToLastValidDieAndReplace(matrix, moveToLocation, this.elementType);
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation, ElementType elementType) {
        matrix.setElementAtIndex((int) moveToLocation.x, (int) moveToLocation.y, elementType.createElementByMatrix((int) moveToLocation.x, (int) moveToLocation.y));
        die(matrix);
    }

    public void moveToLastValidAndSwap(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY, Vector3 moveToLocation) {
        int moveToLocationMatrixX = (int) moveToLocation.x;
        int moveToLocationMatrixY = (int) moveToLocation.y;
        Element thirdNeighbor = matrix.get(moveToLocationMatrixX, moveToLocationMatrixY);

        matrix.setElementAtIndex(this.matrixX, this.matrixY, thirdNeighbor);
        matrix.setElementAtIndex(toSwapX, toSwapY, this);
        matrix.setElementAtIndex(moveToLocationMatrixX, moveToLocationMatrixY, toSwap);
    }

    public void setOwningBodyCoords(Vector2 coords) {
        setOwningBodyCoords((int) coords.x, (int) coords.y);
    }

    public void setOwningBodyCoords(int x, int y) {
        this.owningBodyCoords = new Vector2(x, y);
    }

    public void setCoordinatesByMatrix(Vector2 pos) {
        setCoordinatesByMatrix((int) pos.x, (int) pos.y);
    }

    public void setCoordinatesByMatrix(int providedX, int providedY) {
        setXByMatrix(providedX);
        setYByMatrix(providedY);
    }


    public void setSecondaryCoordinatesByMatrix(int providedX, int providedY) {
        this.secondaryMatrixCoords.add(new Vector2(providedX, providedY));
    }

    public void resetSecondaryCoordinates() {
        this.secondaryMatrixCoords = new ArrayList<>();
    }

    public void setXByMatrix(int providedVal) {
        this.matrixX = providedVal;
        this.pixelX = toPixel(providedVal);
    }

    public void setYByMatrix(int providedVal) {
        this.matrixY = providedVal;
        this.pixelY = toPixel(providedVal);
    }

    public int toMatrix(int pixelVal) {
        return (int) Math.floor(pixelVal / CellularAutomaton.pixelSizeModifier);
    }

    public int toPixel(int matrixVal) {
        return (int) Math.floor(matrixVal * CellularAutomaton.pixelSizeModifier);
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
                        neighbor.receiveHeat(matrix, heatFactor);
                    }
                }
            }
        }
        return true;
    }

    public boolean shouldApplyHeat() {
        return isIgnited || heated;
    }

    public boolean receiveHeat(CellularMatrix matrix, int heat) {
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

    public void checkIfIgnited() {
        if (this.flammabilityResistance <= 0) {
            this.isIgnited = true;
            modifyColor();
        } else {
            this.isIgnited = false;
            this.color = ColorConstants.getColorForElementType(elementType, this.matrixX, this.matrixY);
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
        this.isDead = true;
        Element newElement = type.createElementByMatrix(matrixX, matrixY);
        matrix.setElementAtIndex(matrixX, matrixY, newElement);
        matrix.reportToChunkActive(matrixX, matrixY);
        if (owningBody != null) {
            owningBody.elementDeath(this, newElement);
            secondaryMatrixCoords.forEach(vector2 -> matrix.setElementAtIndex((int) vector2.x, (int) vector2.y, ElementType.EMPTYCELL.createElementByMatrix(0, 0)));
        }
    }

    public void dieAndReplace(CellularMatrix matrix, ElementType type) {
        die(matrix, type);
    }

    public void dieAndReplaceWithParticle(CellularMatrix matrix, Vector3 velocity) {
        matrix.setElementAtIndex(matrixX, matrixY, ElementType.createParticleByMatrix(matrix, matrixX, matrixY, velocity, elementType, this.color, this.isIgnited));
        matrix.reportToChunkActive(matrixX, matrixY);
    }

    public boolean didNotMove(Vector3 formerLocation) {
        return formerLocation.x == matrixX && formerLocation.y == matrixY;
    }

    public boolean hasNotMovedBeyondThreshold() {
        return stoppedMovingCount >= stoppedMovingThreshold;
    }

    public void takeEffectsDamage(CellularMatrix matrix) {
        if (!isEffectsFrame()) {
            return;
        }
        if (isIgnited) {
            takeFireDamage(matrix);
        }
        checkIfDead(matrix);
    }

    public void takeFireDamage(CellularMatrix matrix) {
        health -= fireDamage;
        if (isSurrounded(matrix)) {
            flammabilityResistance = resetFlammabilityResistance;
        }
        checkIfIgnited();
    }

//    private boolean isSurrounded(CellularMatrix matrix) {
//        List<Class> elementList = new ArrayList<>();
//        elementList.add(EmptyCell.class);
//        return isSurrounded(matrix, elementList);
//    }

    public boolean stain(Color color) {
        if (Math.random() > 0.2 || isIgnited) {
            return false;
        }
        this.color = color.cpy();
        this.discolored = true;
        return true;
    }

    public boolean stain(float r, float g, float b, float a) {
        if (Math.random() > 0.2 || isIgnited) {
            return false;
        }
        this.color = this.color.cpy();
        this.color.r += r;
        this.color.g += g;
        this.color.b += b;
        this.color.a += a;
        if (this.color.r > 1f) {
            this.color.r = 1f;
        }
        if (this.color.g > 1f) {
            this.color.g = 1f;
        }
        if (this.color.b > 1f) {
            this.color.b = 1f;
        }
        if (this.color.a > 1f) {
            this.color.a = 1f;
        }
        if (this.color.r < 0f) {
            this.color.r = 0f;
        }
        if (this.color.g < 0f) {
            this.color.g = 0f;
        }
        if (this.color.b < 0f) {
            this.color.b = 0f;
        }
        if (this.color.a < 0f) {
            this.color.a = 0f;
        }
        this.discolored = true;
        return true;
    }

    public boolean cleanColor() {
        if (!discolored || Math.random() > 0.2f) {
            return false;
        }
        this.color = ColorConstants.getColorForElementType(this.elementType, this.matrixX, this.matrixY);
        this.discolored = false;
        return true;
    }

    public boolean explode(CellularMatrix matrix, int strength) {
        if (explosionResistance < strength) {
            if (Math.random() > 0.3) {
                dieAndReplace(matrix, ElementType.EXPLOSIONSPARK);
            } else {
                die(matrix);
            }
            return true;
        } else {
            darkenColor();
            return false;
        }
    }

    public void darkenColor() {
        this.color = new Color(this.color.r * .85f, this.color.g * .85f, this.color.b * .85f, this.color.a);
        this.discolored = true;
    }

    public void darkenColor(float factor) {
        this.color = new Color(this.color.r * factor, this.color.g * factor, this.color.b * factor, this.color.a);
        this.discolored = true;
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

    public void magmatize(CellularMatrix matrix, int damage) {
        this.health -= damage;
        checkIfDead(matrix);
    }

    public boolean infect(CellularMatrix matrix) {
        if (Math.random() > 0.95f) {
            this.dieAndReplace(matrix, ElementType.SLIMEMOLD);
            return true;
        }
        return false;
    }

    public boolean isDead() {
        return isDead;
    }
}
