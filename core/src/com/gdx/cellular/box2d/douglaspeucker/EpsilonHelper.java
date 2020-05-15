package com.gdx.cellular.box2d.douglaspeucker;

import java.util.List;

public class EpsilonHelper {

    /**
     * For each 3 consecutive points in the list this function calculates the distance
     * from the middle point to a line defined by the first and third point.
     *
     * The result may be used to find a proper epsilon by calculating
     * maximum {@link #max(double[])} or average {@link #avg(double[])} from
     * all deviations.
     */
    public static <P extends Point> double[] deviations(List<P> points) {
        double[] deviations = new double[Math.max(0, points.size() - 2)];
        for (int i = 2; i < points.size(); i++) {
            P p1 = points.get(i-2);
            P p2 = points.get(i-1);
            P p3 = points.get(i);
            double dev = new Line<P>(p1, p3).distance(p2);
            deviations[i-2] = dev;
        }
        return deviations;
    }

    public static double sum(double[] values) {
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }


    public static double avg(double[] values) {
        if (values.length > 0) {
            return sum(values)/values.length;
        } else {
            return 0.0;
        }
    }

    public static double max(double[] values) {
        double max = 0.0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }
}
