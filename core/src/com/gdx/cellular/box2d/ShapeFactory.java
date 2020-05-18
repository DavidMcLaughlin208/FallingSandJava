package com.gdx.cellular.box2d;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.box2d.douglaspeucker.Point;
import com.gdx.cellular.box2d.douglaspeucker.PointImpl;
import com.gdx.cellular.box2d.douglaspeucker.SeriesReducer;
import com.gdx.cellular.elements.Element;

import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.decompose.SweepLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShapeFactory {

    private World world;
    private static ShapeFactory shapeFactory;
    private static final EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
    private static final DelaunayTriangulator delaunayTriangulator = new DelaunayTriangulator();

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

    public static Body createDynamicBox(int x, int y, int size, float density, float friction, float restitution) {
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
        fixtureDef.restitution = restitution;

        Fixture fixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(new Vector2(0, -10));

        box.dispose();
        return body;
    }

    public static Body createDynamicPolygonFromElementArray(int x, int y, Array<Array<Element>> elements, boolean earClip) {
        int mod = CellularAutomaton.box2dSizeModifier;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        int xWidth = (elements.get(0).size/2);
        int yWidth = (elements.size / 2);

        List<List<Vector2>> verts = getOutliningVertices(elements);
//        List<Vector2> allVerts = new ArrayList<>(verts.get(0));
//        allVerts.addAll(verts.get(1));

        List<Point> leftPoints = verts.get(0).stream().map(PointImpl::new).collect(Collectors.toList());
        List<Point> reducedLeftPoints = SeriesReducer.reduce(leftPoints, 0.000001f);

        List<Point> rightPoints = verts.get(1).stream().map(PointImpl::new).collect(Collectors.toList());
        List<Point> reducedRightPoints = SeriesReducer.reduce(rightPoints, 0.000001f);

        Vector2 center = new Vector2((float) (xWidth + x) / mod, (float) (yWidth + y) / mod);

        bodyDef.position.set(center);

        Body body = shapeFactory.world.createBody(bodyDef);

        List<Vector2> reducedVerts = new ArrayList<>();
        for (Point reducedLeftPoint : reducedLeftPoints) {
            reducedVerts.add(reducedLeftPoint.getPosition());
        }
        for (Point point : reducedRightPoints) {
            reducedVerts.add(point.getPosition());
        }
        reducedVerts = reducedVerts.stream().map(vector2 -> new Vector2(vector2.x - xWidth, vector2.y - yWidth)).collect(Collectors.toList());
        org.dyn4j.geometry.Vector2[] dyn4jVerts = reducedVerts.stream().map(vec -> new org.dyn4j.geometry.Vector2(vec.x, vec.y)).toArray(org.dyn4j.geometry.Vector2[]::new);
        SweepLine sweepLine = new SweepLine();
        List<Convex> convexes = sweepLine.decompose(dyn4jVerts);

        for (Convex convex : convexes) {
            org.dyn4j.geometry.Polygon polygon = (org.dyn4j.geometry.Polygon) convex;
            List<Triangle> triangles = sweepLine.triangulate(polygon.getVertices());
            for (Triangle triangle : triangles) {
                Vector2[] triangleVerts = Arrays.stream(triangle.getVertices()).map(vert -> new Vector2((float) vert.x, (float) vert.y)).toArray(Vector2[]::new);
                PolygonShape polygonForFixture = new PolygonShape();
                polygonForFixture.set(triangleVerts);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = polygonForFixture;
                fixtureDef.density = 1;
                fixtureDef.friction = 0.8f;
                body.createFixture(fixtureDef);
                polygonForFixture.dispose();
            }
        }

//        ShortArray shortArray;
//        if (earClip) {
//            shortArray = ShortArray.with(earClippingTriangulator.computeTriangles(toFloatArray(reducedVerts)).toArray());
//        } else {
//            float[] vertsAsFloats = toFloatArray(reducedVerts);
//            shortArray = ShortArray.with(delaunayTriangulator.computeTriangles(vertsAsFloats, true).toArray());
////            delaunayTriangulator.trim(shortArray, vertsAsFloats, vertsAsFloats, 0, reducedVerts.size() * 2);
//        }

//        for (int i = 0; i < shortArray.size; i += 3) {
//            PolygonShape polygon = new PolygonShape();
//            Vector2[] triangleVerts = new Vector2[] {
//                    reducedVerts.get(shortArray.items[i]),
//                    reducedVerts.get(shortArray.items[i + 1]),
//                    reducedVerts.get(shortArray.items[i + 2])
//            };
//            polygon.set(triangleVerts);
//            FixtureDef fixtureDef = new FixtureDef();
//            fixtureDef.shape = polygon;
//            fixtureDef.density = 1;
//            fixtureDef.friction = 0.8f;
//            body.createFixture(fixtureDef);
//            polygon.dispose();
//        }

        body.setAngularVelocity((float) (Math.random() * 2));

        return body;
    }

    private static float[] toFloatArray(List<Vector2> reducedVerts) {
        float[] floatArray = new float[reducedVerts.size() * 2];
        for (int i = 0; i < reducedVerts.size(); i++) {
            Vector2 vert = reducedVerts.get(i);
            floatArray[i * 2] = vert.x;
            floatArray[(i * 2) + 1] = vert.y;
        }
        return floatArray;
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
        bodyDef.type = BodyDef.BodyType.DynamicBody;

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
        updatedVertices = updatedVertices.stream().map(vert -> new Vector2(vert.x - center.x, vert.y - center.y)).collect(Collectors.toList());
        Vector2[] verticesAsArray = updatedVertices.toArray(new Vector2[0]);
//        box.setAsBox(10, 10);
        box.set(verticesAsArray);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1;
        fixtureDef.friction = friction;

        Fixture fixture = body.createFixture(fixtureDef);

        box.dispose();
        return body;
    }

    public static void clearAllActors() {
        Array<Body> bodies = new Array<>();
        shapeFactory.world.getBodies(bodies);
        for(int i = 0; i < bodies.size; i++)
        {
            if(!shapeFactory.world.isLocked())
                shapeFactory.world.destroyBody(bodies.get(i));
        }
    }
}
