package com.gdx.cellular.elements;

import java.util.HashMap;
import java.util.Map;

public enum NeighborLocation {

    UPLEFT,
    UP,
    UPRIGHT,
    RIGHT,
    DOWNRIGHT,
    DOWN,
    DOWNLEFT,
    LEFT;

    private static Map<String, NeighborLocation> neighborMap = new HashMap<>();


    public static NeighborLocation fromInts(int x, int y) {
        return fromString(String.format("%s%s", x, y));
    }

    public static NeighborLocation fromString(String loc) {
        if (neighborMap.isEmpty()) {
            initializeNeighborMap();
        }
        return neighborMap.get(loc);
    }

    private static void initializeNeighborMap() {
        neighborMap.put("-11", UPLEFT);
        neighborMap.put("01", UP);
        neighborMap.put("11", UPRIGHT);
        neighborMap.put("10", RIGHT);
        neighborMap.put("1-1", DOWNRIGHT);
        neighborMap.put("0-1", DOWN);
        neighborMap.put("-1-1", DOWNLEFT);
        neighborMap.put("-10", LEFT);
    }

}
