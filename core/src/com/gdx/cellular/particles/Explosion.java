package com.gdx.cellular.particles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Explosion {

    private final CellularMatrix matrix;
    public int radius;
    public int strength;
    public int matrixX;
    public int matrixY;

    public Explosion(CellularMatrix matrix, int radius, int strength, int matrixX, int matrixY) {
        this.matrix = matrix;
        this.radius = radius;
        this.strength = strength;
        this.matrixX = matrixX;
        this.matrixY = matrixY;
    }

    public List<Explosion> enact() {
        Map<String, String> coordinatesCache = new HashMap<>();
        for (int x = radius; x >= radius * -1; x--) {
            for (int y = radius; y >= radius * -1; y--) {
                if (Math.abs(x) == radius || Math.abs(y) == radius) {
                    iterateBetweenTwoPoints(matrixX, matrixY, matrixX + x, matrixY + y, strength, coordinatesCache, matrix);
                }
            }
        }
        for (int x = radius * -1; x < radius; x++) {
            for (int y = radius * -1; y < radius; y++) {
                if (Math.abs(x) == radius || Math.abs(y) == radius) {
                    iterateBetweenTwoPoints(matrixX, matrixY, matrixX + x, matrixY + y, strength, coordinatesCache, matrix);
                }
            }
        }
        return null;
    }

    private void iterateBetweenTwoPoints(int matrixX, int matrixY, int newX, int newY, int strength, Map<String, String> cache, CellularMatrix matrix) {
        int matrixX1 = matrixX;
        int matrixY1 = matrixY;
        int matrixX2 = newX;
        int matrixY2 = newY;

        // If the two points are the same no need to iterate. Just run the provided function

        int xDiff = matrixX1 - matrixX2;
        int yDiff = matrixY1 - matrixY2;
        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

        int xModifier = xDiff < 0 ? 1 : -1;
        int yModifier = yDiff < 0 ? 1 : -1;

        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);
        float floatFreq = (min == 0 || upperBound == 0) ? 0 : ((float) min / upperBound);
        int freqThreshold = 0;
        float freqCounter = 0;

        int smallerCount = 0;
        for (int i = 0; i <= upperBound; i++) {
            freqCounter += floatFreq;
            boolean thresholdPassed = Math.floor(freqCounter) > freqThreshold;
            if (freq != 0 && thresholdPassed && min >= smallerCount) {
                freqThreshold = (int) Math.floor(freqCounter);
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
            int currentY = matrixY1 + (yIncrease * yModifier);
            int currentX = matrixX1 + (xIncrease * xModifier);
            // If these coordinates have been visited previously we can 'continue if the result was
            // true (explosion not stopped) and break if false (explosion stopped at these coordinates previously)
            String cachedResult = cache.get(String.valueOf(newX) + newY);
            if (cachedResult != null && cachedResult.equals(String.valueOf(true))) {
                continue;
            } else if (cachedResult != null && cachedResult.equals(String.valueOf(false))) {
                break;
            }
            int distance = matrix.distanceBetweenTwoPoints(matrixX1, currentX, matrixY1, currentY);
            if (distance < radius/2 && matrix.isWithinBounds(currentX, currentY)) {
                Element element = matrix.get(currentX, currentY);
                boolean unstopped = element.explode(matrix, strength);
                cache.put(String.valueOf(currentX) + currentY, String.valueOf(unstopped));
                if (!unstopped) {
                    break;
                }
            } else if (distance < radius +  Math.max(radius/10, 1)) {
                Vector2 center = new Vector2(matrixX, matrixY);
                Vector2 newPoint = new Vector2(currentX, currentY);
                newPoint.sub(center).nor();
                matrix.particalizeByMatrix(currentX, currentY, new Vector3(newPoint.x * 200, newPoint.y * 200, 0));
            }
        }
    }
    //Still need to find distance between new points and center to check if need to skip, particalize or explode
}


