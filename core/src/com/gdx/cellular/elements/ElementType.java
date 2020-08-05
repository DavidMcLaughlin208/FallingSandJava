package com.gdx.cellular.elements;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.gas.FlammableGas;
import com.gdx.cellular.elements.gas.Smoke;
import com.gdx.cellular.elements.gas.Spark;
import com.gdx.cellular.elements.gas.Steam;
import com.gdx.cellular.elements.liquid.*;
import com.gdx.cellular.elements.player.PlayerMeat;
import com.gdx.cellular.elements.solid.immoveable.*;
import com.gdx.cellular.elements.solid.movable.*;
import com.gdx.cellular.particles.Particle;

public enum ElementType {
    EMPTYCELL(EmptyCell.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return EmptyCell.getInstance();
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return EmptyCell.getInstance();
        }
    },
    GROUND(Ground.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ground(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Ground(x, y, true);
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
    SNOW(Snow.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Snow(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Snow(x, y, true);
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
    CEMENT(Cement.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Cement(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Cement(x, y, true);
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

    },
    WOOD(Wood.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Wood(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Wood(x, y, true);
        }

    },
    TITANIUM(Titanium.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Titanium(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Titanium(x, y, true);
        }

    },
    SPARK(Titanium.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Spark(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Spark(x, y, true);
        }

    },
    EMBER(Ember.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ember(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Ember(x, y, true);
        }

    },
    LAVA(Lava.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Lava(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Lava(x, y, true);
        }

    },
    COAL(Coal.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Coal(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Coal(x, y, true);
        }

    },
    SMOKE(Smoke.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Smoke(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Smoke(x, y, true);
        }

    },
    FLAMMABLEGAS(FlammableGas.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new FlammableGas(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new FlammableGas(x, y, true);
        }

    },
    BLOOD(Blood.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Blood(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Blood(x, y, true);
        }

    },
    SLIMEMOLD(SlimeMold.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new SlimeMold(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new SlimeMold(x, y, true);
        }

    },
    SLIMEMOLDMOVABLE(SlimeMoldMovable.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new SlimeMoldMovable(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new SlimeMoldMovable(x, y, true);
        }

    },
    STEAM(Steam.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Steam(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new Steam(x, y, true);
        }

    },
    PLAYERMEAT(PlayerMeat.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new PlayerMeat(x, y, false);
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            return new PlayerMeat(x, y, true);
        }

    },
    PARTICLE(Particle.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            throw new IllegalStateException();
        }

        @Override
        public Element createElementByPixel(int x, int y) {
            throw new IllegalStateException();
        }

    };

    public final Class<? extends Element> clazz;

    ElementType(Class<? extends Element> clazz) {
        this.clazz = clazz;
    }

    public abstract Element createElementByMatrix(int x, int y);

    public abstract Element createElementByPixel(int x, int y);

    public static Element createParticleByMatrix(CellularMatrix matrix, int x, int y, Vector3 vector3, ElementType elementType) {
        if (matrix.isWithinBounds(x, y)) {
            Element newElement = new Particle(x, y, false, vector3, elementType);
            matrix.setElementAtIndex(x, y, newElement);
            return newElement;
        }
        return null;
    }
}
