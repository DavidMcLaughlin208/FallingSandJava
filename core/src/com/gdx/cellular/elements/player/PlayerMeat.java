package com.gdx.cellular.elements.player;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;
import com.gdx.cellular.elements.solid.movable.MovableSolid;
import com.gdx.cellular.particles.Particle;
import com.gdx.cellular.player.Player;

public class PlayerMeat extends ImmovableSolid {

    private Player owningPlayer;

    public PlayerMeat(int x, int y) {
        super(x, y);
        mass = 200;
        flammabilityResistance = 100;
        resetFlammabilityResistance = 100;
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        vel = new Vector3(0f, 0f,0f);
    }

    @Override
    public void step(CellularMatrix matrix) {
    }

    private void stepAsPartOfPhysicsBody(CellularMatrix matrix) {
        applyHeatToNeighborsIfIgnited(matrix);
        spawnSparkIfIgnited(matrix);
        modifyColor();
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        return false;
    }

    public boolean stepAsPlayer(CellularMatrix matrix, int xOffset, int yOffset) {
        if (matrix.isWithinBounds(getMatrixX() + xOffset, getMatrixY() + yOffset)) {
            Element neighbor = matrix.get(getMatrixX() + xOffset, getMatrixY() + yOffset);
            if (neighbor instanceof EmptyCell || neighbor instanceof Particle || neighbor instanceof Liquid || neighbor instanceof Gas) {
                return true;
            } else if (neighbor instanceof MovableSolid){
                if (neighbor.isFreeFalling) {
                    return true;
                }
                return false;
            } else if (neighbor instanceof PlayerMeat) {
                PlayerMeat otherMeat = (PlayerMeat) neighbor;
                if (otherMeat.getOwningPlayer() == this.getOwningPlayer()) {
                    return true;
                }
                return false;
            } else if (neighbor instanceof ImmovableSolid) {
                return false;
            }
        }
        return true;
    }

    public boolean moveToLocation(CellularMatrix matrix, int x, int y) {
        matrix.setElementAtIndex(getMatrixX(), getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(getMatrixX(), getMatrixY()));
        matrix.setElementAtIndex(x, y, this);
        return true;
    }

    public Player getOwningPlayer() {
        return owningPlayer;
    }

    public void setOwningPlayer(Player owningPlayer) {
        this.owningPlayer = owningPlayer;
    }
}
