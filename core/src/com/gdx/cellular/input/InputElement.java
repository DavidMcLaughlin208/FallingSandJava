package com.gdx.cellular.input;

import com.badlogic.gdx.Input;
import com.gdx.cellular.elements.ElementType;

import java.util.HashMap;
import java.util.Map;

public class InputElement {

    Map<Integer, ElementType> elementMap;
    private static final InputElement inputElement;
    static {
        inputElement = new InputElement();
    }

    private InputElement() {
        elementMap = new HashMap<>();
        elementMap.put(Input.Keys.NUM_1, ElementType.STONE);
        elementMap.put(Input.Keys.NUM_2, ElementType.SAND);
        elementMap.put(Input.Keys.NUM_3, ElementType.DIRT);
        elementMap.put(Input.Keys.NUM_4, ElementType.WATER);
        elementMap.put(Input.Keys.NUM_5, ElementType.OIL);
        elementMap.put(Input.Keys.NUM_6, ElementType.ACID);
        elementMap.put(Input.Keys.NUM_7, ElementType.WOOD);
        elementMap.put(Input.Keys.NUM_8, ElementType.TITANIUM);
        elementMap.put(Input.Keys.NUM_9, ElementType.EMPTYCELL);
        elementMap.put(Input.Keys.E, ElementType.EMBER);
        elementMap.put(Input.Keys.O, ElementType.COAL);
        elementMap.put(Input.Keys.L, ElementType.LAVA);
        elementMap.put(Input.Keys.B, ElementType.BLOOD);
        elementMap.put(Input.Keys.G, ElementType.FLAMMABLEGAS);
        elementMap.put(Input.Keys.F, ElementType.SPARK);
        elementMap.put(Input.Keys.N, ElementType.SNOW);
        elementMap.put(Input.Keys.M, ElementType.SLIMEMOLD);
    }

    public static ElementType getElementForKeycode(int keycode) {
        return inputElement.elementMap.get(keycode);
    }
}
