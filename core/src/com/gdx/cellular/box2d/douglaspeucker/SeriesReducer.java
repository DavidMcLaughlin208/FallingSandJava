package com.gdx.cellular.box2d.douglaspeucker;

import java.util.ArrayList;
import java.util.List;

public class SeriesReducer {

    /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
     *
     * @param points
     *          initial, ordered list of points (objects implementing the {@link Point} interface)
     * @param epsilon
     *          allowed margin of the resulting curve, has to be > 0
     */
    public static <P extends Point> List<P> reduce(List<P> points, double epsilon) {
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        double furthestPointDistance = 0.0;
        int furthestPointIndex = 0;
        Line<P> line = new Line<P>(points.get(0), points.get(points.size() - 1));
        for (int i = 1; i < points.size() - 1; i++) {
            double distance = line.distance(points.get(i));
            if (distance > furthestPointDistance ) {
                furthestPointDistance = distance;
                furthestPointIndex = i;
            }
        }
        if (furthestPointDistance > epsilon) {
            List<P> reduced1 = reduce(points.subList(0, furthestPointIndex+1), epsilon);
            List<P> reduced2 = reduce(points.subList(furthestPointIndex, points.size()), epsilon);
            List<P> result = new ArrayList<P>(reduced1);
            result.addAll(reduced2.subList(1, reduced2.size()));
            return result;
        } else {
            return line.asList();
        }
    }

}
