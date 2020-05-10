package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;

import java.util.*;
import java.util.stream.Collectors;

public class ColorConstants {

    private static final Map<ElementType, List<Color>> elementColorMap = new HashMap<>();
    private static final Map<String, List<Color>> effectsColorMap = new HashMap<>();
    private static final Random random = new Random();

    // Movable Solids
    private static final Color SAND_1 = new Color(255/255f, 255/255f, 0/255f, 1);
    private static final Color SAND_2 = new Color(178/255f, 201/255f, 6/255f, 1);
    private static final Color SAND_3 = new Color(233/255f, 252/255f, 90/255f, 1);

    private static final Color DIRT_1 = new Color(96/255f, 47/255f, 18/255f, 1);
    private static final Color DIRT_2 = new Color(135/255f, 70/255f, 32/255f, 1);
    private static final Color DIRT_3 = new Color(79/255f, 38/255f, 15/255f, 1);

    private static final Color COAL_1 = new Color(53/255f, 53/255f, 53/255f, 1);
    private static final Color COAL_2 = new Color(34/255f, 35/255f, 38/255f, 1);
    private static final Color COAL_3 = new Color(65/255f, 65/255f, 65/255f, 1);

    private static final Color EMBER = new Color(102/255f, 59/255f, 0/255f, 1);

    // Immovable Solids
    private static final Color STONE = new Color(150/255f, 150/255f, 150/255f, 1);

    private static final Color WOOD_1 = new Color(165/255f, 98/255f, 36/255f, 1);
    private static final Color WOOD_2 = new Color(61/255f, 33/255f, 7/255f, 1);
    private static final Color WOOD_3 = new Color(140/255f, 74/255f, 12/255f, 1);

    private static final Color TITANIUM = new Color(234/255f, 234/255f, 234/255f, 1);





    // Liquids
    private static final Color WATER = new Color(28/255f, 86/255f, 234/255f, .8f);

    private static final Color OIL = new Color(55/255f, 60/255f, 73/255f, .8f);

    private static final Color ACID = new Color(0/255f, 255/255f, 0/255f, 1);

    private static final Color LAVA = new Color(255/255f, 165/255f, 0/255f, 1);

    private static final Color BLOOD = new Color(234/255f, 0 /255f,0/255f, .8f);


    // Gasses
    private static final Color SMOKE = new Color(147/255f, 147/255f, 147/255f, 0.5f);

    private static final Color FLAMMABLE_GAS = new Color(0/255f, 255/255f, 0/255f, 0.5f);

    private static final Color SPARK = new Color(89/255f, 35/255f, 13/255f, 1);

    // Effects
    private static final String FIRE_NAME = "Fire";
    private static final Color FIRE_1 = new Color(89/255f, 35/255f, 13/255f, 1);
    private static final Color FIRE_2 = new Color(100/255f, 27/255f, 7/255f, 1);
    private static final Color FIRE_3 = new Color(77/255f, 10/255f, 20/255f, 1);


    private static final Color PARTICLE = new Color(0/255f, 0/255f, 0/255f, 0);
    private static final Color EMPTY_CELL = new Color(0/255f, 0/255f, 0/255f, 0);



    static {
        Arrays.stream(ElementType.values()).forEach(type -> elementColorMap.put(type, new ArrayList<>()));
        elementColorMap.get(ElementType.SAND).add(SAND_1);
        elementColorMap.get(ElementType.SAND).add(SAND_2);
        elementColorMap.get(ElementType.SAND).add(SAND_3);

        elementColorMap.get(ElementType.DIRT).add(DIRT_1);
        elementColorMap.get(ElementType.DIRT).add(DIRT_2);
        elementColorMap.get(ElementType.DIRT).add(DIRT_3);

        elementColorMap.get(ElementType.COAL).add(COAL_1);
        elementColorMap.get(ElementType.COAL).add(COAL_2);
        elementColorMap.get(ElementType.COAL).add(COAL_3);

        elementColorMap.get(ElementType.EMBER).add(EMBER);

        elementColorMap.get(ElementType.STONE).add(STONE);

        elementColorMap.get(ElementType.WOOD).add(WOOD_1);
        elementColorMap.get(ElementType.WOOD).add(WOOD_2);
        elementColorMap.get(ElementType.WOOD).add(WOOD_3);

        elementColorMap.get(ElementType.TITANIUM).add(TITANIUM);

        elementColorMap.get(ElementType.WATER).add(WATER);

        elementColorMap.get(ElementType.OIL).add(OIL);

        elementColorMap.get(ElementType.ACID).add(ACID);

        elementColorMap.get(ElementType.LAVA).add(LAVA);

        elementColorMap.get(ElementType.BLOOD).add(BLOOD);

        elementColorMap.get(ElementType.SMOKE).add(SMOKE);

        elementColorMap.get(ElementType.FLAMMABLEGAS).add(FLAMMABLE_GAS);

        elementColorMap.get(ElementType.SPARK).add(SPARK);

        elementColorMap.get(ElementType.PARTICLE).add(PARTICLE);

        elementColorMap.get(ElementType.EMPTYCELL).add(EMPTY_CELL);

        effectsColorMap.put(FIRE_NAME, new ArrayList<>());
        effectsColorMap.get(FIRE_NAME).add(FIRE_1);
        effectsColorMap.get(FIRE_NAME).add(FIRE_2);
        effectsColorMap.get(FIRE_NAME).add(FIRE_3);



        List<ElementType> missingElements = Arrays.stream(ElementType.values()).filter(type -> elementColorMap.get(type).size() == 0).collect(Collectors.toList());
        if (missingElements.size() > 0) {
            throw new IllegalStateException("Elements " + missingElements.toString() + "have no assigned colors");
        }
    }

    public static Color getColorForElementType(ElementType elementType) {
        List<Color> colorList = elementColorMap.get(elementType);
        return elementColorMap.get(elementType).get(random.nextInt(colorList.size()));
    }


}
