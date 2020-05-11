package com.gdx.cellular.util;

import com.badlogic.gdx.math.Vector3;

public class Chunk {

    public static int size = 32;

    private boolean shouldStep = true;
    private boolean shouldStepNextFrame = true;
    private Vector3 topLeft;
    private Vector3 bottomRight;

    public Chunk(Vector3 topLeft, Vector3 bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public Chunk() {

    }

    public void setTopLeft(Vector3 topLeft) {
        this.topLeft = topLeft;
    }

    public Vector3 getTopLeft() {
        return topLeft;
    }

    public void setShouldStep(boolean shouldStep) {
        this.shouldStep = shouldStep;
    }

    public boolean getShouldStep() {
        return this.shouldStep;
    }

    public void setShouldStepNextFrame(boolean shouldStepNextFrame) {
        this.shouldStepNextFrame = shouldStepNextFrame;
    }

    public boolean getShouldStepNextFrame() {
        return this.shouldStepNextFrame;
    }

    public void setBottomRight(Vector3 bottomRight) {
        this.bottomRight = bottomRight;
    }

    public Vector3 getBottomRight() {
        return bottomRight;
    }

    public void shiftShouldStepAndReset() {
        this.shouldStep = this.shouldStepNextFrame;
        this.shouldStepNextFrame = false;
    }
}
