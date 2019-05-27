package com.gdx.cellular.elements;

import com.gdx.cellular.Cell;

public enum ElementType {
    EMPTY_CELL(EmptyCell.class) {
        @Override
        public Element create(Cell cell) {
            return null;
        }
    },
    STONE(Stone.class) {
        @Override
        public Element create(Cell cell) {
            return new Stone(cell);
        }
    },
    SAND(Sand.class) {
        @Override
        public Element create(Cell cell) {
            return new Sand(cell);
        }
    },
    DIRT(Dirt.class) {
        @Override
        public Element create(Cell cell) {
            return new Dirt(cell);
        }
    },
    WATER(Water.class) {
        @Override
        public Element create(Cell cell) {
            return new Water(cell);
        }

    },
    OIL(Oil.class) {
        @Override
        public Element create(Cell cell) {
            return new Oil(cell);
        }

    };

    public final Class clazz;

    ElementType(Class clazz) {
        this.clazz = clazz;
    }

    public abstract  Element create(Cell cell);
}
