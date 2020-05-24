package com.gdx.cellular.box2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.movable.MovableSolid;

public class PhysicsElementActor {

    Array<Array<Element>> elements;
    Body physicsBody;
    Vector2 lastPos = new Vector2(0,0);
    float xAccumulator = 0;
    float yAccumulator = 0;
    float angleAccumulator = 0;
    float lastAngle = 0;
    int xCenterOffset;
    int yCenterOffset;
    int shouldCalculateCount = 3;

    public PhysicsElementActor(Body body, Array<Array<Element>> elements, int minX, int maxY) {
        this.physicsBody = body;
        this.elements = elements;
        xCenterOffset = (int) Math.abs(body.getPosition().x*5 - minX);
        yCenterOffset = (int) Math.abs(body.getPosition().y*5 - maxY);
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    element.owningBody = this;
                    element.setOwningBodyCoords(x - xCenterOffset, y - yCenterOffset);
                }
            }
        }
    }

    public void step(CellularMatrix matrix) {
        if (Math.abs(physicsBody.getWorldCenter().y) > 200 || Math.abs(physicsBody.getWorldCenter().x) > 200) {
            matrix.destroyPhysicsElementActor(this);
            return;
        }
        xAccumulator += Math.abs(physicsBody.getPosition().x - lastPos.x);
        yAccumulator += Math.abs(physicsBody.getPosition().y - lastPos.y);
        angleAccumulator += Math.abs(physicsBody.getAngle() - lastAngle);
        if (shouldCalculateCount > 0 || xAccumulator > .1 || yAccumulator > .1 | angleAccumulator > .5) {
            for (int y = 0; y < elements.size; y++) {
                Array<Element> row = elements.get(y);
                for (int x = 0; x < row.size; x++) {
                    Element element = row.get(x);
                    if (element != null) {
                        if (element.secondaryMatrixCoords.size() > 0) {
                            element.secondaryMatrixCoords.forEach(vector2 -> matrix.setElementAtIndex((int) vector2.x, (int) vector2.y, ElementType.EMPTYCELL.createElementByMatrix(0,0)));
                            element.resetSecondaryCoordinates();
                        }
                        Vector2 matrixCoords = getMatrixCoords(element);
                        Element elementAtNewPos = matrix.get((int) matrixCoords.x, (int) matrixCoords.y);
                        if (elementAtNewPos == element) {
                            continue;
                        }
                        if (elementAtNewPos == null) {
                            matrix.setElementAtIndex(element.matrixX, element.matrixY, ElementType.EMPTYCELL.createElementByMatrix(element.matrixX, element.matrixY));
                        }
                        if (elementAtNewPos != null && elementAtNewPos.owningBody != null) {
                            elementAtNewPos.owningBody.shouldCalculateCount = 2;
                        }
                        if (elementAtNewPos instanceof EmptyCell || (elementAtNewPos != null && elementAtNewPos.owningBody == this)) {
                            matrix.setElementAtIndex(element.matrixX, element.matrixY, ElementType.EMPTYCELL.createElementByMatrix(element.matrixX, element.matrixY));
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
                        } else if (elementAtNewPos instanceof MovableSolid || elementAtNewPos instanceof Liquid) {
                            elementAtNewPos.dieAndReplaceWithParticle(matrix, matrix.generateRandomVelocityWithBounds(-100, 100));
                            physicsBody.setLinearVelocity(physicsBody.getLinearVelocity().scl(.9f));
                            physicsBody.setAngularVelocity(physicsBody.getAngularVelocity() * .98f);
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
                        } else {
                            matrix.setElementAtIndex(element.matrixX, element.matrixY, ElementType.EMPTYCELL.createElementByMatrix(element.matrixX, element.matrixY));
                        }
                        //                    if (elementAtNewPos instanceof MovableSolid) {
                        //                        if (matrix.isWithinBounds(matrixCoords)) {
                        //                            matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                        //                        }
                        //                        elementAtNewPos.dieAndReplaceWithParticle(matrix, matrix.generateRandomVelocityWithBounds(-150, 150));
                        //                        physicsBody.setLinearVelocity(physicsBody.getLinearVelocity().scl(.9f));
                        //                        physicsBody.setAngularVelocity(physicsBody.getAngularVelocity() * .98f);
                        //                    }
                    }
                }
            }
            this.lastAngle = physicsBody.getAngle();
            this.lastPos = physicsBody.getPosition().cpy();
            xAccumulator = 0;
            yAccumulator = 0;
            angleAccumulator = 0;
            shouldCalculateCount -= 1;
            int drawLength = 2;
            for (int y = 0; y < elements.size; y++) {
                Array<Element> row = elements.get(y);
                for (int x = 0; x < row.size - drawLength; x++) {
                    Element element = row.get(x);
                    if (element != null) {
                        for (int length = 1; length <= drawLength; length++) {
                            Element nextElement = row.get(x + length);
                            if ((element.matrixX - nextElement.matrixX != length)) {
                                matrix.setElementAtSecondLocation(element.matrixX + length, element.matrixY, element);
                            }
                        }
                    }
                }
            }
        } else {
            this.lastAngle = physicsBody.getAngle();
            this.lastPos = physicsBody.getPosition().cpy();
        }
    }

    public void draw(ShapeRenderer sr) {
//        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        int mod = CellularAutomaton.pixelSizeModifier;
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x ++) {
                Element element = row.get(x);
                if (element != null) {
                    int pixelX = element.toPixel(element.matrixX);
                    int pixelY = element.toPixel(element.matrixY);
                    sr.setColor(element.color);
                    sr.rect(element.toPixel(element.matrixX), element.toPixel(element.matrixY), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
                    if (element.secondaryMatrixCoords.size() > 0) {
                        element.secondaryMatrixCoords.forEach(vector2 -> sr.rect(element.toPixel((int) vector2.x), element.toPixel((int) vector2.y), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier));
                    }
//                    sr.rect(pixelX, pixelY, pixelX + 1, pixelY - 1, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier, 1, 1, (float) Math.toDegrees(physicsBody.getAngle()));
                }
            }
        }
//        sr.end();
    }

    public Vector2 getMatrixCoords(Element element) {
        Vector2 bodyPos = physicsBody.getPosition();
        int bodyCenterMatrixX = (int) ((bodyPos.x * CellularAutomaton.box2dSizeModifier)/2);
        int bodyCenterMatrixY = (int) ((bodyPos.y * CellularAutomaton.box2dSizeModifier)/2);
        Vector2 matrixPoint = new Vector2(bodyCenterMatrixX + element.owningBodyCoords.x, bodyCenterMatrixY - element.owningBodyCoords.y);
        float angle = physicsBody.getAngle();
        float new_x = (float) (((matrixPoint.x-bodyCenterMatrixX) * Math.cos(angle) - (matrixPoint.y-bodyCenterMatrixY) * Math.sin(angle)) + bodyCenterMatrixX);
        float new_y = (float) (((matrixPoint.y-bodyCenterMatrixY) * Math.cos(angle) + (matrixPoint.x-bodyCenterMatrixX) * Math.sin(angle)) + bodyCenterMatrixY);
        matrixPoint.x = Math.round(new_x);
        matrixPoint.y = Math.round(new_y);
        return matrixPoint;
    }

    public boolean elementDeath(Element elementToDie, Element replacement) {
        this.elements.get((int) elementToDie.owningBodyCoords.y + yCenterOffset).set((int) elementToDie.owningBodyCoords.x + xCenterOffset, replacement);
        if (replacement != null) {
            replacement.owningBody = this;
            replacement.setOwningBodyCoords(elementToDie.owningBodyCoords);
        }
        return true;
    }

}
