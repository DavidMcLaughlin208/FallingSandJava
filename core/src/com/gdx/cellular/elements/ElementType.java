package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.gas.*;
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
    },
    GROUND(Ground.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ground(x, y, false);
        }
    },
    STONE(Stone.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Stone(x, y, false);
        }
    },
    SAND(Sand.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Sand(x, y, false);
        }
    },
    SNOW(Snow.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Snow(x, y, false);
        }
    },
    DIRT(Dirt.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Dirt(x, y, false);
        }
    },
    WATER(Water.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Water(x, y, false);
        }
    },
    CEMENT(Cement.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Cement(x, y, false);
        }
    },
    OIL(Oil.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Oil(x, y, false);
        }
    },
    ACID(Acid.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Acid(x, y, false);
        }
    },
    WOOD(Wood.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Wood(x, y, false);
        }
    },
    TITANIUM(Titanium.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Titanium(x, y, false);
        }
    },
    SPARK(Spark.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Spark(x, y, false);
        }
    },
    EXPLOSIONSPARK(ExplosionSpark.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new ExplosionSpark(x, y, false);
        }
    },
    EMBER(Ember.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ember(x, y, false);
        }
    },
    LAVA(Lava.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Lava(x, y, false);
        }
    },
    COAL(Coal.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Coal(x, y, false);
        }
    },
    SMOKE(Smoke.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Smoke(x, y, false);
        }
    },
    FLAMMABLEGAS(FlammableGas.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new FlammableGas(x, y, false);
        }
    },
    BLOOD(Blood.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Blood(x, y, false);
        }
    },
    SLIMEMOLD(SlimeMold.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new SlimeMold(x, y, false);
        }
    },
    STEAM(Steam.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Steam(x, y, false);
        }
    },
    PLAYERMEAT(PlayerMeat.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new PlayerMeat(x, y, false);
        }
    },
    PARTICLE(Particle.class) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            throw new IllegalStateException();
        }
    };

    public final Class<? extends Element> clazz;

    ElementType(Class<? extends Element> clazz) {
        this.clazz = clazz;
    }

    public abstract Element createElementByMatrix(int x, int y);

    public static Element createParticleByMatrix(CellularMatrix matrix, int x, int y, Vector3 vector3, ElementType elementType, Color color, boolean isIgnited) {
        if (matrix.isWithinBounds(x, y)) {
            Element newElement = new Particle(x, y, false, vector3, elementType, color, isIgnited);
            matrix.setElementAtIndex(x, y, newElement);
            return newElement;
        }
        return null;
    }
}
