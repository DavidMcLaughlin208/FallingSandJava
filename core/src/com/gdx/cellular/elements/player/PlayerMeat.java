package com.gdx.cellular.elements.player;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;

public class PlayerMeat extends ImmovableSolid {

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
        if (this.owningBody != null) {
            stepAsPartOfPhysicsBody(matrix);
        }
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
}
