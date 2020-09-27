package com.gdx.cellular.box2d;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.box2d.linesimplification.Visvalingam;
import com.gdx.cellular.box2d.marchingsquares.MooreNeighborTracing;
import com.gdx.cellular.elements.Element;

import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.decompose.EarClipping;
import org.dyn4j.geometry.decompose.SweepLine;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.geomgraph.index.SweepLineEvent;
import org.locationtech.jts.index.sweepline.SweepLineOverlapAction;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class ShapeFactory {

    private World world;
    private static ShapeFactory shapeFactory;
    private static final EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
    private static final DelaunayTriangulator delaunayTriangulator = new DelaunayTriangulator();
    private static final SweepLine sweepLine = new SweepLine();

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

    public static Body createDynamicPolygonFromElementArray(int x, int y, Array<Array<Element>> elements) {
        return createPolygonFromElementArray(x, y, elements, BodyDef.BodyType.DynamicBody);
    }

    public static Body createStaticPolygonFromElementArray(int x, int y, Array<Array<Element>> elements) {
        return createPolygonFromElementArray(x, y, elements, BodyDef.BodyType.StaticBody);
    }

    public static Body createPolygonFromElementArrayDeleteOldBody(int x, int y, Array<Array<Element>> elements, Body body) {
        Body newBody = createPolygonFromElementArray(x, y, elements, body.getType());
        if (newBody == null) return null;
        shapeFactory.world.destroyBody(body);
        return newBody;
    }

    // This method takes a 2D array of elements and performs the following steps.
    // Finds the outlining vertices
    // Converts them to Box2D scale
    // Uses Douglas-Peucker simplification to remove unnecessary vertices
    // Uses a SweepLine algorithm to break the polygon into 1 or more convex polygons
    // Uses a triangulation algorithm
    // Creates Box2D Fixtures from the triangles and adds them to the body
    public static Body createPolygonFromElementArray(int x, int y, Array<Array<Element>> elements, BodyDef.BodyType shapeType) {
        int mod = CellularAutomaton.box2dSizeModifier/2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = shapeType;
        int xWidth = (elements.get(0).size/2);
        int yWidth = (elements.size / 2);

        Vector2 center = new Vector2((float) (xWidth + x) / mod, (float) (yWidth + y) / mod);

        bodyDef.position.set(center);
        Body body = shapeFactory.world.createBody(bodyDef);

        elements.reverse();
        List<Vector2> allVerts = MooreNeighborTracing.getOutliningVerts(elements);
        elements.reverse();
        List<Vector2> allVertsTransformed = allVerts.stream().map(vector2 -> new Vector2((vector2.x - xWidth)/(mod), (vector2.y - yWidth)/(mod))).collect(Collectors.toList());
//        List<Vector2> simplifiedVerts = Visvalingam.simplify(allVerts);
//        simplifiedVerts = simplifiedVerts.stream().map(vector2 -> new Vector2((vector2.x - xWidth)/(mod), (vector2.y - yWidth)/(mod))).collect(Collectors.toList());


// LOCATIONTECH POLYGON GENERATION
        List<Float> earVertsList = new ArrayList<>();
        allVertsTransformed.forEach(vector2 -> {
            earVertsList.add(vector2.x);
            earVertsList.add(vector2.y);
        });
        earVertsList.add(allVertsTransformed.get(0).x);
        earVertsList.add(allVertsTransformed.get(0).y);
        float[] earVerts = new float[earVertsList.size()];
        for(int i = 0; i < earVertsList.size(); ++i) {
            earVerts[i] = earVertsList.get(i);
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = new PackedCoordinateSequence.Float(earVerts, 2, 0);
        if (!(coordinateSequence.size() == 0 || coordinateSequence.size() >= 4)) {
            return body;
        }
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = geometryFactory.createPolygon(linearRing);

        final Geometry simplifiedPolygon = DouglasPeuckerSimplifier.simplify(polygon, .3);

        List<org.dyn4j.geometry.Vector2> dyn4jVerts = Arrays.stream(simplifiedPolygon.getCoordinates()).map(vec -> new org.dyn4j.geometry.Vector2(vec.x, vec.y)).collect(Collectors.toList());
        if (dyn4jVerts.size() <= 2) {
            return null;
        }
        dyn4jVerts.remove(dyn4jVerts.size() - 1);
        List<Convex> convexes;
        if (dyn4jVerts.size() == 3) {
            Convex convex = new org.dyn4j.geometry.Polygon((org.dyn4j.geometry.Vector2) dyn4jVerts);
            convexes = new ArrayList<>();
            convexes.add(convex);
        } else if (dyn4jVerts.size() > 3) {
            try {
                convexes = sweepLine.decompose(dyn4jVerts.toArray(new org.dyn4j.geometry.Vector2[0]));
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }

        for (Convex convex : convexes) {
            org.dyn4j.geometry.Polygon dynConvexPolygon = (org.dyn4j.geometry.Polygon) convex;
            org.dyn4j.geometry.Vector2[] dyn4jConvexVerts = dynConvexPolygon.getVertices();
            float[] convexVerts = new float[dyn4jConvexVerts.length * 2 + 2];
            for(int i = 0; i < dyn4jConvexVerts.length; ++i) {
                convexVerts[i * 2] = (float) dyn4jConvexVerts[i].x;
                convexVerts[i * 2 + 1] = (float) dyn4jConvexVerts[i].y;
            }
            convexVerts[convexVerts.length - 2] = (float) dyn4jConvexVerts[0].x;
            convexVerts[convexVerts.length - 1] = (float) dyn4jConvexVerts[0].y;

            CoordinateSequence convexCoordinateSequence = new PackedCoordinateSequence.Float(convexVerts, 2, 0);
            LinearRing convexLinearRing = new LinearRing(convexCoordinateSequence, geometryFactory);
            Polygon ltConvexPolygon = geometryFactory.createPolygon(convexLinearRing);

            DelaunayTriangulationBuilder triangulationBuilder = new DelaunayTriangulationBuilder();
            triangulationBuilder.setSites(ltConvexPolygon);
            Geometry triangulatedGeometry = triangulationBuilder.getTriangles(geometryFactory);
            int geometryCount = triangulatedGeometry.getNumGeometries();

            for (int i = 0; i < geometryCount; i++) {
                Geometry currentGeometry = triangulatedGeometry.getGeometryN(i);
                Coordinate[] coordinates = currentGeometry.getCoordinates();
                Vector2[] triangleVerts = new Vector2[3];
                for (int c = 0; c < 3; c++) {
                    Coordinate currentCoordinate = coordinates[c];
                    Vector2 transformedCoordinate = new Vector2();
                    transformedCoordinate.x = (float) currentCoordinate.x;
                    transformedCoordinate.y = (float) currentCoordinate.y;
                    triangleVerts[c] = transformedCoordinate;
                }
                PolygonShape polygonForFixture = new PolygonShape();
                polygonForFixture.set(triangleVerts);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = polygonForFixture;
                fixtureDef.density = 5;
                fixtureDef.friction = 1f;
                fixtureDef.restitution = 0.1f;
                body.createFixture(fixtureDef);
                polygonForFixture.dispose();
            }
        }


//  LIBGDX POLYGON GENERATION
//        DelaunayTriangulator triangulator = new DelaunayTriangulator();
//        List<Float> earVertsList = new ArrayList<>();
//        simplifiedVerts.forEach(vector2 -> {
//            earVertsList.add(vector2.x);
//            earVertsList.add(vector2.y);
//        });
//        float[] earVerts = new float[earVertsList.size()];
//        for(int i = 0; i < earVertsList.size(); ++i) {
//            earVerts[i] = earVertsList.get(i);
//        }
//        ShortArray shortArray = triangulator.computeTriangles(earVerts, false);
//
//        for (int i = 0; i < shortArray.size - 3; i++) {
//            Vector2[] triangleVerts = new Vector2[3];
//            triangleVerts[0] = simplifiedVerts.get(shortArray.get(i));
//            triangleVerts[1] = simplifiedVerts.get(shortArray.get(i + 1));
//            triangleVerts[2] = simplifiedVerts.get(shortArray.get(i + 2));
//            float area = Math.abs(((triangleVerts[0].x * (triangleVerts[1].y - triangleVerts[2].y)) + (triangleVerts[1].x * (triangleVerts[2].y - triangleVerts[0].y)) + (triangleVerts[2].x * (triangleVerts[0].y - triangleVerts[1].y))) / 2f);
//            if (area == 0.0f) {
//                continue;
//            }
//            PolygonShape polygonForFixture = new PolygonShape();
//            polygonForFixture.set(triangleVerts);
//            FixtureDef fixtureDef = new FixtureDef();
//            fixtureDef.shape = polygonForFixture;
//            fixtureDef.density = 5;
//            fixtureDef.friction = 1f;
//            fixtureDef.restitution = 0.1f;
//            body.createFixture(fixtureDef);
//            polygonForFixture.dispose();
//        }


// DYN4J POLYGON GENERATION
//        simplifiedVerts = simplifiedVerts.stream().map(vector2 -> new Vector2((vector2.x - xWidth)/(mod), (vector2.y - yWidth)/(mod))).collect(Collectors.toList());
//        org.dyn4j.geometry.Vector2[] dyn4jVerts = simplifiedVerts.stream().map(vec -> new org.dyn4j.geometry.Vector2(vec.x, vec.y)).toArray(org.dyn4j.geometry.Vector2[]::new);
//        SweepLine sweepLine = new SweepLine();
//        List<Convex> convexes;
//
//        if (dyn4jVerts.length > 3) {
//            convexes = sweepLine.decompose(dyn4jVerts);
//        } else if (dyn4jVerts.length == 3) {
//            Convex convex = new org.dyn4j.geometry.Polygon(dyn4jVerts);
//            convexes = new ArrayList<>();
//            convexes.add(convex);
//        } else {
//            return null;
//        }
//
//        for (Convex convex : convexes) {
//            org.dyn4j.geometry.Polygon polygon = (org.dyn4j.geometry.Polygon) convex;
//            org.dyn4j.geometry.Vector2[] convexVerts = polygon.getVertices();
//            List<Triangle> triangles = new ArrayList<>();
//            if (convexVerts.length == 3) {
//                triangles.add(new Triangle(convexVerts[0], convexVerts[1], convexVerts[2]));
//            } else {
//                triangles = sweepLine.triangulate(convexVerts);
//            }
//            for (Triangle triangle : triangles) {
//                Vector2[] triangleVerts = Arrays.stream(triangle.getVertices()).map(vert -> new Vector2((float) vert.x, (float) vert.y)).toArray(Vector2[]::new);
//                float area = Math.abs(((triangleVerts[0].x * (triangleVerts[1].y - triangleVerts[2].y)) + (triangleVerts[1].x * (triangleVerts[2].y - triangleVerts[0].y)) + (triangleVerts[2].x * (triangleVerts[0].y - triangleVerts[1].y))) / 2f);
//                if (area < .08) {
//                    continue;
//                }
//                PolygonShape polygonForFixture = new PolygonShape();
//                polygonForFixture.set(triangleVerts);
//                FixtureDef fixtureDef = new FixtureDef();
//                fixtureDef.shape = polygonForFixture;
//                fixtureDef.density = 5;
//                fixtureDef.friction = 1f;
//                fixtureDef.restitution = 0.1f;
//                body.createFixture(fixtureDef);
//                polygonForFixture.dispose();
//            }
//        }

//        body.setAngularVelocity((float) (Math.random() * 2));

        return body;
    }

    private static org.dyn4j.geometry.Vector2[] removeDuplicateVerts(org.dyn4j.geometry.Vector2[] convexVerts) {
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0;i < convexVerts.length; i++) {
            for (int k = i + 1; k < convexVerts.length; k++) {
                if (k!=i && convexVerts[k].equals(convexVerts[i])) {
                    toRemove.add(0, i);
                }
            }
        }
        List<org.dyn4j.geometry.Vector2> list = new ArrayList<>(Arrays.asList(convexVerts));
        toRemove.forEach(index -> list.remove(index));
        org.dyn4j.geometry.Vector2[] newVerts = new org.dyn4j.geometry.Vector2[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newVerts[i] = list.get(i);
        }
        return newVerts;
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
            for (int x = 0; x < row.size; x++) {
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
        return createRect(boxCenter, vertices, 0, 0.5f, BodyDef.BodyType.StaticBody);
    }

    public static Body createDynamicRect(Vector3 boxCenter, List<Vector2> vertices) {
        return createRect(boxCenter, vertices, 0, 0.5f, BodyDef.BodyType.DynamicBody);
    }

    public static Body createBoxByBodyType(Vector3 boxCenter, List<Vector2> vertices, BodyDef.BodyType type) {
        return createRect(boxCenter, vertices, 0, 0.5f, type);
    }

    public static Body createRect(Vector3 boxCenter, List<Vector2> vertices, int angle, float friction, BodyDef.BodyType type) {
        int mod = CellularAutomaton.box2dSizeModifier/2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;

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
        org.dyn4j.geometry.Vector2[] dyn4jVerts = updatedVertices.stream().map(vec -> new org.dyn4j.geometry.Vector2(vec.x, vec.y)).toArray(org.dyn4j.geometry.Vector2[]::new);
        SweepLine sweepLine = new SweepLine();
        List<Convex> convexes;

        if (dyn4jVerts.length > 3) {
            convexes = sweepLine.decompose(dyn4jVerts);
        } else {
            Convex convex = new org.dyn4j.geometry.Polygon(dyn4jVerts);
            convexes = new ArrayList<>();
            convexes.add(convex);
        }

        for (Convex convex : convexes) {
            org.dyn4j.geometry.Polygon polygon = (org.dyn4j.geometry.Polygon) convex;
            List<Triangle> triangles = sweepLine.triangulate(polygon.getVertices());
            for (Triangle triangle : triangles) {
                Vector2[] triangleVerts = Arrays.stream(triangle.getVertices()).map(vert -> new Vector2((float) vert.x, (float) vert.y)).toArray(Vector2[]::new);
                float area = Math.abs(((triangleVerts[0].x * (triangleVerts[1].y - triangleVerts[2].y)) + (triangleVerts[1].x * (triangleVerts[2].y - triangleVerts[0].y)) + (triangleVerts[2].x * (triangleVerts[0].y - triangleVerts[1].y))) / 2f);
                if (area < .00001) {
                    continue;
                }
                PolygonShape polygonForFixture = new PolygonShape();
                polygonForFixture.set(triangleVerts);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = polygonForFixture;
                fixtureDef.density = 10;
                fixtureDef.friction = 0.8f;
                fixtureDef.restitution = 0.2f;
                body.createFixture(fixtureDef);
                polygonForFixture.dispose();
            }
        }
        body.setTransform(body.getPosition(), angle);
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
