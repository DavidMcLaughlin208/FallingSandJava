package com.gdx.cellular.box2d.douglaspeucker;

import com.badlogic.gdx.math.Vector2;

public class PointImpl implements Point {

    private double x;

    private double y;

    private Vector2 position;

    public PointImpl(double x, double y) {
        this.x = x;
        this.y = y;
        this.position = new Vector2((float) x, (float) y);
    }

    public PointImpl(Vector2 position) {
        this.x = position.x;
        this.y = position.y;
        this.position = position;
    }

    public static Point p(double x, double y) {
        return new PointImpl(x, y);
    }

    public static Point p(Vector2 position) {
        return new PointImpl(position);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PointImpl other = (PointImpl) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
