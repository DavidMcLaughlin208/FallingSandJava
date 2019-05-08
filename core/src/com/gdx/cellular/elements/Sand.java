package com.gdx.cellular.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;

public class Sand extends Element implements Solid {

    public Sand(int x, int y, boolean isPixel) {
        super(x, y, isPixel);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = 1f;
    }

    public Color color = Color.YELLOW;

    @Override
    public void draw(ShapeRenderer sr) {
        sr.setColor(color);
        sr.rect(pixelX, pixelY, CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
    }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.add(CellularAutomaton.gravity);

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        int velYDeltaTime = (int) (Math.abs(vel.y) * Gdx.graphics.getDeltaTime());
        int velXDeltaTime = (int) (Math.abs(vel.x) * Gdx.graphics.getDeltaTime());

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

        int smallerCount = 0;
        Vector2 lastValidLocation = new Vector2(matrixX, matrixY);
        for (int i = 1; i <= upperBound; i++) {
            if (freq != 0 && i % freq == 0 && min <= smallerCount) {
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

            int modifiedMatrixY = matrixY + (yIncrease * yModifier);
            int modifiedMatrixX = matrixX + (xIncrease * xModifier);
            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
                if (neighbor == this) continue;
                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x =  matrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTY_CELL.createElementByPixel(pixelX, pixelY));
            }
        }
    }

    private boolean actOnNeighboringElement(Element neighbor, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector2 lastValidLocation, int depth) {
        if (neighbor instanceof EmptyCell) {
            if (isFinal) {
                swapPositions(matrix, neighbor);
            } else {
                return false;
            }
        } else if (neighbor instanceof Liquid) {
            swapPositions(matrix, neighbor);
            return true;
//        } else if (neighbor instanceof Solid) {
//            if (isFinal) {
//                moveToLastValid(matrix, lastValidLocation);
//                return true;
//            }
//            int additionalX = 0;
//            int additionalY = 0;
//            Vector3 normalizedVel = vel.nor();
//            additionalY = normalizedVel.y == 0 ? (Math.random() > .5 ? 1 : -1) : (int) normalizedVel.y;
//            additionalX = normalizedVel.x == 0 ? (Math.random() > .5 ? 1 : -1) : (int) normalizedVel.x;
//            if (normalizedVel.x > .2f) {
//                additionalX = (int) Math.ceil(normalizedVel.x);
//            } else if (normalizedVel.x < -.2f) {
//                additionalX = (int) Math.floor(normalizedVel.x);
//            } else {
//                additionalX = (Math.random() > .5 ? 1 : -1);
//            }
//            int currentMatrixX = (int) (lastValidLocation.x + additionalX);
//            int currentMatrixY = (int) (lastValidLocation.y + additionalY);
//            Element diagonalNeighbor = matrix.get(currentMatrixX, currentMatrixY);
//            if (diagonalNeighbor != null) {
//                boolean isSolid = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation);
//                if (isSolid){
//                    additionalX *= -.01f;
//                    return true;
//                }
////                    vel.x = Math.abs(vel.y) * vel.x;
//            }
//            vel.y = Math.min(-62f, (vel.y + neighbor.vel.y) / 2) + additionalY * 62;
//            float xVelIncrease = additionalX * neighbor.frictionFactor;
//            boolean increaseIsLarger = Math.abs(xVelIncrease) > Math.abs(vel.x);
//            vel.x = increaseIsLarger ? xVelIncrease * frictionFactor : vel.x;
//            return true;
        } else if (neighbor instanceof Solid) {
            if (depth > 0) {
                vel.x *= -1;
                return true;
            }
            if (isFirst) {
                Vector3 normalizedVel = vel.nor();
                int additionalX = getAdditional(normalizedVel.x);
                int additionalY = getAdditional(normalizedVel.y);
                Element diagonalNeighbor = matrix.get(matrixX + additionalX, matrixY + additionalY);
                if (diagonalNeighbor != null) {
                    boolean stopped = actOnNeighboringElement(diagonalNeighbor, matrix, true, false, lastValidLocation, depth + 1);
                    //boolean stopped = iterateBetweenTwoPointsAndAct((int) lastValidLocation.x, (int) lastValidLocation.y, additionalX, additionalY, matrix);
                    if (stopped) {
                        vel.x = (-1 * additionalX * 62f);
                        vel.y = -62f;
                        return true;
                    } else {
                        vel.y = -62f;
                        vel.x = additionalX * 62f;
                    }
                }
            } else {
                moveToLastValid(matrix, lastValidLocation);
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
                neighbor.vel.y = vel.y;
//                vel.x = (vel.x + neighbor.vel.x) / 2;
                return true;
            }
        }
        return false;
    }

//    private boolean iterateBetweenTwoPointsAndAct(int matrixX, int matrixY, int xFactor, int yFactor, CellularMatrix matrix) {
//        int yModifier = yFactor < 0 ? -1 : 1;
//        int xModifier = yFactor < 0 ? -1 : 1;
//
//        boolean xDiffIsLarger = Math.abs(xFactor) > Math.abs(yFactor);
//
//        int upperBound = Math.max(Math.abs(xFactor), Math.abs(yFactor));
//        int min = Math.min(Math.abs(xFactor), Math.abs(yFactor));
//        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);
//
//        int smallerCount = 0;
//        Vector2 lastValidLocation = new Vector2(matrixX, matrixY);
//        for (int i = 1; i <= upperBound; i++) {
//            if (freq != 0 && i % freq == 0 && min <= smallerCount) {
//                smallerCount += 1;
//            }
//
//            int yIncrease, xIncrease;
//            if (xDiffIsLarger) {
//                xIncrease = i;
//                yIncrease = smallerCount;
//            } else {
//                yIncrease = i;
//                xIncrease = smallerCount;
//            }
//
//            int modifiedMatrixY = matrixY + (yIncrease * yModifier);
//            int modifiedMatrixX = matrixX + (xIncrease * xModifier);
//            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
//                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
//                if (neighbor == this) continue;
//                boolean stopped = actOnNeighboringElement(neighbor, matrix, i == upperBound, false, lastValidLocation, 0);
//                if (stopped) {
//                    return stopped;
//                }
//                lastValidLocation.x =  matrixX;
//                lastValidLocation.y = modifiedMatrixY;
//
//            } else {
//                matrix.setElementAtIndex(matrixX, matrixY, ElementType.EMPTY_CELL.createElementByPixel(pixelX, pixelY));
//                return true;
//            }
//        }
//        return true;
//    }

    private int getAdditional(float val) {
        if (val < 0) {
            return 1 * (int) Math.floor(val);
        } else if (val > 0) {
            return 1 * (int) Math.ceil(val);
        } else {
            return Math.random() > 0.5 ? 0 : (Math.random() > 0.5 ? 1 : -1);
        }
    }

    private float getAverageVelOrGravity(float vel, float otherVel) {
        float avg = (vel + otherVel) / 2;
        if (avg > 0) {
            return avg;
        } else {
            return Math.min(avg, -62f);
        }
    }
}
