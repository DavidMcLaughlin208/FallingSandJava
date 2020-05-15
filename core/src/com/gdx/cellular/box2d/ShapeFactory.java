package com.gdx.cellular.box2d;

import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.box2d.douglaspeucker.Point;
import com.gdx.cellular.box2d.douglaspeucker.PointImpl;
import com.gdx.cellular.box2d.douglaspeucker.SeriesReducer;
import com.gdx.cellular.elements.Element;
import com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static Body createDynamicPolygonFromElementArray(int x, int y, Array<Array<Element>> elements) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        List<List<Vector2>> verts = getOutliningVertices(elements);
        List<Point> leftPoints = verts.get(0).stream().map(PointImpl::new).collect(Collectors.toList());
        List<Point> reducedLeftPoints = SeriesReducer.reduce(leftPoints, 0.1f);
        List<Point> rightPoints = verts.get(1).stream().map(PointImpl::new).collect(Collectors.toList());
        List<Point> reducedRightPoints = SeriesReducer.reduce(rightPoints, 0.1f);
        Vector2 center = new Vector2((float) ((elements.get(0).size/2) + x) / mod, (float) ((elements.size / 2) + y) / mod);

        bodyDef.position.set(center);

        Body body = shapeFactory.world.createBody(bodyDef);

        PolygonShape polygon = new PolygonShape();
        List<Vector2> reducedVerts = new ArrayList<>();
        reducedLeftPoints.forEach(point -> reducedVerts.add(point.getPosition()));
        reducedRightPoints.forEach(point -> reducedVerts.add(point.getPosition()));
        Vector2[] vertArray = reducedVerts.toArray(new Vector2[0]);
        polygon.set(vertArray);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygon;
        fixtureDef.density = 1;
        fixtureDef.friction = 0.8f;

        Fixture fixture = body.createFixture(fixtureDef);

        polygon.dispose();
        return body;
    }

    private static List<List<Vector2>> getOutliningVertices(Array<Array<Element>> elements) {
        List<Vector2> leftSideVertices = new ArrayList<>();
        List<Vector2> rightSideVertices = new ArrayList<>(elements.size);
        boolean foundFirst;
        Vector2 mostRecentElementPos = null;
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            foundFirst = false;
            for (int x = 0; x < elements.get(0).size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    if (!foundFirst) {
                        leftSideVertices.add(new Vector2(x, y));
                        foundFirst = true;
                    } else {
                        mostRecentElementPos = new Vector2(x, y);
                    }
                }
            }
            if (mostRecentElementPos != null) {
                rightSideVertices.add(0, mostRecentElementPos.cpy());
                mostRecentElementPos = null;
            }

        }
        List<List<Vector2>> outliningVerts = new ArrayList<>();
        outliningVerts.add(new ArrayList<>(leftSideVertices));
        outliningVerts.add(new ArrayList<>(rightSideVertices));
        return outliningVerts;
    }

    public static Body createDefaultDynamicBox(int x, int y, int size) {
        return createDynamicBox(x, y, size, 0.5f, 0.4f, 0.1f);
    }

    public static Body createStaticRect(Vector3 boxCenter, List<Vector2> vertices) {
        return createStaticRect(boxCenter, vertices, 0, 0.5f);
    }

    public static Body createStaticRect(Vector3 boxCenter, List<Vector2> vertices, int angle, float friction) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        Vector2 center = new Vector2(Math.round(boxCenter.x / mod), Math.round(boxCenter.y / mod));
        bodyDef.position.set(center);

        Body body = shapeFactory.world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        List<Vector2> updatedVertices = vertices.stream().map(v -> v.scl(1f/(float) mod)).collect(Collectors.toList());
        updatedVertices = vertices.stream().map(v -> {
            v.x = (float) Math.floor(v.x);
            v.y = (float) Math.floor(v.y);
            return v;
        }).collect(Collectors.toList());
        Vector2[] verticesAsArray = updatedVertices.toArray(new Vector2[0]);
        box.set(verticesAsArray);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1;
        fixtureDef.friction = friction;

        Fixture fixture = body.createFixture(fixtureDef);

        box.dispose();
        return body;
    }
}
