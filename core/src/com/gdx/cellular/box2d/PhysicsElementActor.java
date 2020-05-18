package com.gdx.cellular.box2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.movable.MovableSolid;

public class PhysicsElementActor {

    Array<Array<Element>> elements;
    Body physicsBody;

    public PhysicsElementActor(Body body, Array<Array<Element>> elements) {
        this.physicsBody = body;
        this.elements = elements;
        int xWidth = (elements.get(0).size/2);
        int yWidth = (elements.size / 2);
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x ++) {
                Element element = row.get(x);
                if (element != null) {
                    element.owningBody = body;
                    element.setOwningBodyCoords(x - xWidth, y -  yWidth);
                }
            }
        }
    }

    public void step(CellularMatrix matrix) {
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x ++) {
                Element element = row.get(x);
                if (element != null) {
                    Vector2 matrixCoords = getMatrixCoords(element);
                    Element elementAtNewPos = matrix.get((int) matrixCoords.x, (int) matrixCoords.y);
                    if (elementAtNewPos instanceof EmptyCell || (elementAtNewPos != null && elementAtNewPos.owningBody == physicsBody)) {
                        if (matrix.isWithinBounds(matrixCoords)) {
                            matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                        }
                    }
                    if (elementAtNewPos instanceof MovableSolid) {
                        elementAtNewPos.dieAndReplaceWithParticle(matrix, matrix.generateRandomVelocityWithBounds(-150, 150));
                    }
                }
            }
        }
    }

    public void draw(ShapeRenderer sr) {
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x ++) {
                Element element = row.get(x);
                if (element != null) {
                    Vector2 matrixCoords = getMatrixCoords(element);
                    sr.setColor(element.color);
                    sr.rect(element.toPixel(element.matrixX), element.toPixel(element.matrixY), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
                }
            }
        }
        sr.end();
    }

    public Vector2 getMatrixCoords(Element element) {
        Vector2 bodyPos = physicsBody.getPosition();
        return new Vector2((bodyPos.x * CellularAutomaton.box2dSizeModifier) + element.owningBodyCoords.x, (bodyPos.y  * CellularAutomaton.box2dSizeModifier) - element.owningBodyCoords.y);
    }
}
