package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.boids.Boid;
import com.gdx.cellular.elements.gas.*;
import com.gdx.cellular.elements.liquid.*;
import com.gdx.cellular.elements.player.PlayerMeat;
import com.gdx.cellular.elements.solid.immoveable.*;
import com.gdx.cellular.elements.solid.movable.*;
import com.gdx.cellular.particles.Particle;

import java.util.*;
import java.util.stream.Collectors;

public enum ElementType {
    EMPTYCELL(EmptyCell.class, ClassType.EMPTYCELL) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return EmptyCell.getInstance();
        }
    },
    GROUND(Ground.class, ClassType.IMMOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ground(x, y);
        }
    },
    STONE(Stone.class, ClassType.IMMOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Stone(x, y);
        }
    },
    SAND(Sand.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Sand(x, y);
        }
    },
    SNOW(Snow.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Snow(x, y);
        }
    },
    DIRT(Dirt.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Dirt(x, y);
        }
    },
    GUNPOWDER(Gunpowder.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Gunpowder(x, y);
        }
    },
    WATER(Water.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Water(x, y);
        }
    },
    CEMENT(Cement.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Cement(x, y);
        }
    },
    OIL(Oil.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Oil(x, y);
        }
    },
    ACID(Acid.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Acid(x, y);
        }
    },
    WOOD(Wood.class, ClassType.IMMOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Wood(x, y);
        }
    },
    TITANIUM(Titanium.class, ClassType.IMMOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Titanium(x, y);
        }
    },
    SPARK(Spark.class, ClassType.GAS) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Spark(x, y);
        }
    },
    EXPLOSIONSPARK(ExplosionSpark.class, ClassType.GAS) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new ExplosionSpark(x, y);
        }
    },
    EMBER(Ember.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Ember(x, y);
        }
    },
    LAVA(Lava.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Lava(x, y);
        }
    },
    COAL(Coal.class, ClassType.MOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Coal(x, y);
        }
    },
    SMOKE(Smoke.class, ClassType.GAS) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Smoke(x, y);
        }
    },
    FLAMMABLEGAS(FlammableGas.class, ClassType.GAS) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new FlammableGas(x, y);
        }
    },
    BLOOD(Blood.class, ClassType.LIQUID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Blood(x, y);
        }
    },
    SLIMEMOLD(SlimeMold.class, ClassType.IMMOVABLESOLID) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new SlimeMold(x, y);
        }
    },
    STEAM(Steam.class, ClassType.GAS) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new Steam(x, y);
        }
    },
    PLAYERMEAT(PlayerMeat.class, ClassType.PLAYER) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            return new PlayerMeat(x, y);
        }
    },
    PARTICLE(Particle.class, ClassType.PARTICLE) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            throw new IllegalStateException();
        }
    },
    BOID(Boid.class, ClassType.PARTICLE) {
        @Override
        public Element createElementByMatrix(int x, int y) {
            throw new IllegalStateException();
        }
    };

    public final Class<? extends Element> clazz;
    public final ClassType classType;
    public static List<ElementType> IMMOVABLE_SOLIDS;
    public static List<ElementType> MOVABLE_SOLIDS;
    public static List<ElementType> SOLIDS;
    public static List<ElementType> LIQUIDS;
    public static List<ElementType> GASSES;

    ElementType(Class<? extends Element> clazz, ClassType classType) {
        this.clazz = clazz;
        this.classType = classType;
    }

    public abstract Element createElementByMatrix(int x, int y);

    public static Element createParticleByMatrix(CellularMatrix matrix, int x, int y, Vector3 vector3, ElementType elementType, Color color, boolean isIgnited) {
        if (matrix.isWithinBounds(x, y)) {
            Element newElement = new Particle(x, y, vector3, elementType, color, isIgnited);
            matrix.setElementAtIndex(x, y, newElement);
            return newElement;
        }
        return null;
    }

    public static Boid createBoidByMatrix(CellularMatrix matrix, int x, int y, Vector3 velocity) {
        if (matrix.isWithinBounds(x, y)) {
            Boid boid = new Boid(x, y, velocity);
            matrix.addBoid(boid);
            matrix.setElementAtIndex(x, y, boid);
            return boid;
        }
        return null;
    }

    public static List<ElementType> getMovableSolids() {
        if (MOVABLE_SOLIDS == null) {
            MOVABLE_SOLIDS = initializeList(ClassType.MOVABLESOLID);
            MOVABLE_SOLIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(MOVABLE_SOLIDS);
    }

    public static List<ElementType> getImmovableSolids() {
        if (IMMOVABLE_SOLIDS == null) {
            IMMOVABLE_SOLIDS = initializeList(ClassType.IMMOVABLESOLID);
            IMMOVABLE_SOLIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(IMMOVABLE_SOLIDS);
    }

    public static List<ElementType> getSolids() {
        if (SOLIDS == null) {
            List<ElementType> immovables = new ArrayList<>(getImmovableSolids());
            immovables.addAll(getMovableSolids());
            SOLIDS = immovables;
            immovables.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(SOLIDS);
    }

    public static List<ElementType> getLiquids() {
        if (LIQUIDS == null) {
            LIQUIDS = initializeList(ClassType.LIQUID);
            LIQUIDS.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(LIQUIDS);
    }

    public static List<ElementType> getGasses() {
        if (GASSES == null) {
            GASSES = initializeList(ClassType.GAS);
            GASSES.sort(Comparator.comparing(Enum::toString));
        }
        return Collections.unmodifiableList(GASSES);
    }

    private static List<ElementType> initializeList(ClassType classType) {
        return Arrays.stream(ElementType.values()).filter(elementType -> elementType.classType.equals(classType)).collect(Collectors.toList());
    }


    public static Element createParticleByMatrix(CellularMatrix matrix, int x, int y, Vector3 vector3, Element sourceElement) {
        if (matrix.isWithinBounds(x, y)) {
            Element newElement = new Particle(x, y, vector3, sourceElement);
            matrix.setElementAtIndex(x, y, newElement);
            return newElement;
        }
        return null;
    }

    public enum ClassType {
        MOVABLESOLID,
        IMMOVABLESOLID,
        LIQUID,
        GAS,
        PARTICLE,
        EMPTYCELL,
        PLAYER;

    }
}
