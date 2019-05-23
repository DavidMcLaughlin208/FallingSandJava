package com.gdx.cellular.elements;

import com.gdx.cellular.Cell;

public enum ElementType {
    EMPTY_CELL(EmptyCell.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new EmptyCell(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new EmptyCell(x, y, true);
//        }
        @Override
        public Element create(Cell cell) {
            return null;
        }
    },
    STONE(Stone.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new Stone(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new Stone(x, y, true);
//        }
        @Override
        public Element create(Cell cell) {
            return new Stone(cell);
        }
    },
    SAND(Sand.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new Sand(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new Sand(x, y, true);
//        }
        @Override
        public Element create(Cell cell) {
            return new Sand(cell);
        }
    },
    DIRT(Dirt.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new Dirt(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new Dirt(x, y, true);
//        }
        @Override
        public Element create(Cell cell) {
            return new Dirt(cell);
        }
    },
    WATER(Water.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new Water(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new Water(x, y, true);
//        }
        @Override
        public Element create(Cell cell) {
            return new Water(cell);
        }

    },
    OIL(Oil.class) {
//        @Override
//        public Element createElementByMatrix(int x, int y) {
//            return new Oil(x, y, false);
//        }
//
//        @Override
//        public Element createElementByPixel(int x, int y) {
//            return new Oil(x, y, true);
//        }

        @Override
        public Element create(Cell cell) {
            return new Oil(cell);
        }

    };

    public final Class clazz;

    ElementType(Class clazz) {
        this.clazz = clazz;
    }

//    public abstract Element createElementByMatrix(int x, int y);

//    public abstract Element createElementByPixel(int x, int y);

    public abstract  Element create(Cell cell);
}
