package com.gdx.cellular.box2d.douglaspeucker;

import com.badlogic.gdx.math.Vector2;

/**
 * Represents a point on a plane. A point consists of 2 coordinates - x and y.
 */
public interface Point {

    double getX();

    double getY();

    Vector2 getPosition();
}
