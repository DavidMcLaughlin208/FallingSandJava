package com.gdx.cellular.elements;

public enum ElementType {
    EMPTY_CELL(EmptyCell.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new EmptyCell(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new EmptyCell(x, y, true);
        }
    },
    STONE(Stone.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Stone(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Stone(x, y, true);
        }
    },
    SAND(Sand.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Sand(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Sand(x, y, true);
        }
    },
    DIRT(Dirt.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Dirt(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Dirt(x, y, true);
        }
    },
    WATER(Water.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Water(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Water(x, y, true);
        }

    },
    OIL(Oil.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Oil(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Oil(x, y, true);
        }

    },
    ACID(Acid.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Acid(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Acid(x, y, true);
        }

    };

    public final Class clazz;

    ElementType(Class clazz) {
        this.clazz = clazz;
    }

    public abstract Element createElementByMatrix(int x, int y);

    public abstract Element createElementByPixel(int x, int y);

}
