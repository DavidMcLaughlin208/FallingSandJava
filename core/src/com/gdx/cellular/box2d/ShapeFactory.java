package com.gdx.cellular.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.gdx.cellular.CellularAutomaton;

public class ShapeFactory {

    private World world;
    private static ShapeFactory shapeFactory;

    private ShapeFactory(World world) {
        this.world = world;
    }

    public static void initialize(World world) {
        if (shapeFactory == null) {
            shapeFactory = new ShapeFactory(world);
        }
    }

    public static Body createDefaultDynamicCircle(int x, int y, int radius) {
        return createDynamicCircle(x, y, radius, 0.5f, 0.4f, 0.6f);
    }

    public static Body createDynamicCircle(int x, int y, int radius, float density, float friction, float restituion) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;


        bodyDef.position.set((float) x / mod, (float) y/ mod);

        Body body = shapeFactory.world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(radius);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restituion;

        Fixture fixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vector2(0, -10));

        circle.dispose();
        return body;
    }

    public static Body createDynamicBox(int x, int y, int size, float density, float friction, float restituion) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;


        bodyDef.position.set((float) x / mod, (float) y/mod);

        Body body = shapeFactory.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(size, size);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restituion;

        Fixture fixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vector2(0, -10));

        box.dispose();
        return body;
    }

    public static Body createDefaultDynamicBox(int x, int y, int size) {
        return createDynamicBox(x, y, size, 0.5f, 0.4f, 0.1f);
    }
}
