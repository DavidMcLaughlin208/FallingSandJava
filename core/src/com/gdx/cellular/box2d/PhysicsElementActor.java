package com.gdx.cellular.box2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.movable.MovableSolid;

public class PhysicsElementActor {

    Array<Array<Element>> elements;
    Body physicsBody;
    Vector2 lastPos = new Vector2(0,0);
    float xAccumulator = 0;
    float yAccumulator = 0;
    float angleAccumulator = 0;
    float lastAngle = 0;
    int xWidth;
    int yWidth;

    public PhysicsElementActor(Body body, Array<Array<Element>> elements) {
        this.physicsBody = body;
        this.elements = elements;
//        xWidth = (elements.get(0).size/2);
        Vector2 offsetVector = new Vector2();
        ((PolygonShape) physicsBody.getFixtureList().get(0).getShape()).getVertex(0, offsetVector);
        xWidth = (int) offsetVector.x;
//        yWidth = (elements.size / 2);
        yWidth = (int) offsetVector.y;
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    element.owningBody = this;
                    element.setOwningBodyCoords(x - (xWidth*10), y + (yWidth*10));
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
        if (xAccumulator > .1 || yAccumulator > .1 | angleAccumulator > .5) {
            for (int y = 0; y < elements.size; y++) {
                Array<Element> row = elements.get(y);
                for (int x = 0; x < row.size; x++) {
                    Element element = row.get(x);
                    if (element != null) {
                        if (element.secondaryMatrixX != -1 && element.secondaryMatrixY != -1) {
                            matrix.setElementAtIndex(element.secondaryMatrixX, element.secondaryMatrixY, ElementType.EMPTYCELL.createElementByMatrix(element.secondaryMatrixX, element.secondaryMatrixY));
                            element.setSecondaryCoordinatesByMatrix(-1, -1);
                        }
                        Vector2 matrixCoords = getMatrixCoords(element);
                        Element elementAtNewPos = matrix.get((int) matrixCoords.x, (int) matrixCoords.y);
                        if (elementAtNewPos == element) {
                            continue;
                        }
                        if (elementAtNewPos instanceof EmptyCell || (elementAtNewPos != null && elementAtNewPos.owningBody == this)) {
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex(element.matrixX, element.matrixY, ElementType.EMPTYCELL.createElementByMatrix(element.matrixX, element.matrixY));
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
                        } else {
                            matrix.setElementAtIndex(element.matrixX, element.matrixY, ElementType.EMPTYCELL.createElementByMatrix(element.matrixX, element.matrixY));
                        }
                        if (elementAtNewPos instanceof MovableSolid) {
                            elementAtNewPos.dieAndReplaceWithParticle(matrix, matrix.generateRandomVelocityWithBounds(-150, 150));
                            physicsBody.setLinearVelocity(physicsBody.getLinearVelocity().scl(.9f));
                            physicsBody.setAngularVelocity(physicsBody.getAngularVelocity() * .98f);
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
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
                this.lastAngle = physicsBody.getAngle();
                this.lastPos = physicsBody.getPosition().cpy();
                xAccumulator = 0;
                yAccumulator = 0;
                angleAccumulator = 0;
            }
            for (int y = 0; y < elements.size; y++) {
                Array<Element> row = elements.get(y);
                for (int x = 0; x < row.size - 2; x++) {
                    Element element = row.get(x);
                    if (element != null) {
                        Element nextElement = row.get(x + 1);
                        if (nextElement != null && (element.matrixX - nextElement.matrixX != 1)) {
                            matrix.setElementAtSecondLocation(element.matrixX + 1, element.matrixY, element);
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
                    if (element.secondaryMatrixX != -1 || element.secondaryMatrixY != -1) {
                        sr.rect(element.toPixel(element.secondaryMatrixX), element.toPixel(element.secondaryMatrixY), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
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
        this.elements.get((int) elementToDie.owningBodyCoords.y + yWidth).set((int) elementToDie.owningBodyCoords.x + xWidth, replacement);
        if (replacement != null) {
            replacement.owningBody = this;
            replacement.setOwningBodyCoords(elementToDie.owningBodyCoords);
        }
        return true;
    }

}
