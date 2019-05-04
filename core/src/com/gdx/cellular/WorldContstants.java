package com.gdx.cellular;

import com.badlogic.gdx.math.Vector3;

public final class WorldContstants {

    private WorldContstants() {
        throw new IllegalArgumentException("Should not be instantiated");
    }

    public static Vector3 gravity = new Vector3(0f, -1f, 0f);
}
