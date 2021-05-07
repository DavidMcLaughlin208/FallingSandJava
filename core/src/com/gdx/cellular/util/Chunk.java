package com.gdx.cellular.util;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.boids.Boid;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Chunk {

    public static int size = 64 / CellularAutomaton.pixelSizeModifier;

    private boolean shouldStep = true;
    private boolean shouldStepNextFrame = true;
    private Vector3 topLeft;
    private Vector3 bottomRight;
    private ConcurrentHashMap<Boid, String> boidMap = new ConcurrentHashMap<>();

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

    public void addBoid(Boid boid) {
        this.boidMap.put(boid, "");
    }

    public void removeBoid(Boid boid) {
        this.boidMap.remove(boid);
    }

    public List<Boid> getAllBoids() {
        return new ArrayList<>(this.boidMap.keySet());
    }

    public void removeAllBoids() {
        this.boidMap.clear();
    }
}
