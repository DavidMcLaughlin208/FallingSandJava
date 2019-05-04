package com.gdx.cellular.elements;

public enum ElementType {
    EMPTY_CELL {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new EmptyCell(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new EmptyCell(x, y, true);
        }
    },
    STONE {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Stone(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Stone(x, y, true);
        }
    },
    SAND {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Sand(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Sand(x, y, true);
        }
    };

    public abstract Element createElementByMatrix(int x, int y);

    public abstract Element createElementByPixel(int x, int y);

}
