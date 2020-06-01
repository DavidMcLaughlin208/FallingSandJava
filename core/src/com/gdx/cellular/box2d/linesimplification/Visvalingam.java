package com.gdx.cellular.box2d.linesimplification;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Visvalingam {

    private static final float DEFAULT_THRESHOLD = .4f;

    private Visvalingam() { throw new IllegalStateException("Should not instantiate Visvalingam"); }

    public static List<Vector2> simplify(List<Vector2> verts) {
        return simplify(verts, DEFAULT_THRESHOLD);
    }

    public static List<Vector2> simplify(List<Vector2> verts, float threshold) {
        if (verts.size() <= 3) {
            return verts;
        }
        List<Vector2> simplifiedVerts = new ArrayList<>();
        int skippedCount = 0;
        int vertsSize = verts.size();
        calculateTriangleAreaAndAddPointToVerts(verts.get(vertsSize - 1), verts.get(0), verts.get(1), simplifiedVerts, threshold);
        for (int i = 0; i < verts.size() - 2; i++) {
            Vector2 point1 = verts.get(i);
            Vector2 point2 = verts.get(i + 1);
            Vector2 point3 = verts.get(i + 2);
            boolean added = calculateTriangleAreaAndAddPointToVerts(point1, point2, point3, simplifiedVerts, threshold);
            if (added) {
                if (skippedCount > 20) {
                    int index = simplifiedVerts.size() - 1;
                    if (!(index < 0)) {
                        simplifiedVerts.add(index, verts.get(i - (skippedCount / 2)));
                    }
                }
                skippedCount = 0;
            } else {
                skippedCount++;
            }

        }
        calculateTriangleAreaAndAddPointToVerts(verts.get(vertsSize - 2), verts.get(vertsSize - 1), verts.get(0), simplifiedVerts, threshold);
        return simplifiedVerts;

    }

    private static boolean calculateTriangleAreaAndAddPointToVerts(Vector2 point1, Vector2 point2, Vector2 point3, List<Vector2> simplifiedVerts, float threshold) {
        float area = Math.abs(((point1.x * (point2.y - point3.y)) + (point2.x * (point3.y - point1.y)) + (point3.x * (point1.y - point2.y))) / 2f);
        if (area > threshold) {
            simplifiedVerts.add(point2.cpy());
            return true;
        } else {
            return false;
        }
    }

}
