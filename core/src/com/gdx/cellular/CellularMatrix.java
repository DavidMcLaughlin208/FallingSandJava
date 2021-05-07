package com.gdx.cellular;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.boids.Boid;
import com.gdx.cellular.box2d.PhysicsElementActor;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.ColorConstants;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.player.PlayerMeat;
import com.gdx.cellular.elements.solid.movable.MovableSolid;
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.particles.Explosion;
import com.gdx.cellular.spouts.ElementSpout;
import com.gdx.cellular.spouts.ParticleSpout;
import com.gdx.cellular.spouts.Spout;
import com.gdx.cellular.util.Chunk;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class CellularMatrix {

    public int innerArraySize;
    public int outerArraySize;
    public int pixelSizeModifier;
    private final List<Integer> shuffledXIndexes;
    public boolean useChunks = true;
    public int drawThreadCount = 6;
    private List<List<Integer>> shuffledXIndexesForThreads;
    private List<List<Integer>> shuffledYIndexesForThreads;
    private int threadedIndexOffset = 0;

    private Array<Array<Element>> matrix;
    private final Array<Array<Chunk>> chunks;
    public Array<Spout> spoutArray;
    public Array<PhysicsElementActor> physicsElementActors = new Array<>();
    public World world;
    public Array<Explosion> explosionArray = new Array<>();
    public Array<Boid> boids = new Array<>();

    public CellularMatrix(int width, int height, int pixelSizeModifier, World world) {
        this.pixelSizeModifier = pixelSizeModifier;
        this.innerArraySize = toMatrix(width);
        this.outerArraySize = toMatrix(height);
        this.matrix = generateMatrix();
        this.world = world;
        this.chunks = generateChunks();
        this.shuffledXIndexes = generateShuffledIndexes(innerArraySize);

        calculateAndSetThreadedXIndexOffset();
        spoutArray = new Array<>();
    }

    private Array<Array<Chunk>> generateChunks() {
        Array<Array<Chunk>> chunks = new Array<>();
        int rows = (int) Math.ceil((double) outerArraySize / Chunk.size);
        int columns = (int) Math.ceil((double) innerArraySize / Chunk.size);
        for (int r = 0; r < rows; r++) {
            chunks.add(new Array<>());
            for (int c = 0; c < columns; c++) {
                int xPos = c * Chunk.size;
                int yPos = r * Chunk.size;
                Chunk newChunk = new Chunk();
                chunks.get(r).add(newChunk);
                newChunk.setTopLeft(new Vector3(Math.min(xPos, innerArraySize), Math.min(yPos, outerArraySize), 0));
                newChunk.setBottomRight(new Vector3(Math.min(xPos + Chunk.size, innerArraySize), Math.min(yPos + Chunk.size, outerArraySize), 0));
            }
        }
        return chunks;
    }

    public void calculateAndSetThreadedXIndexOffset() {
        if (shuffledXIndexesForThreads != null) {
            threadedIndexOffset = (int) (Math.random() * (innerArraySize / shuffledXIndexesForThreads.size()));
        } else {
            threadedIndexOffset = 0;
        }
    }

    private Array<Array<Element>> generateMatrix() {
        Array<Array<Element>> outerArray = new Array<>(true, outerArraySize);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> innerArr = new Array<>(true, innerArraySize);
            for (int x = 0; x < innerArraySize; x++) {
                innerArr.add(ElementType.EMPTYCELL.createElementByMatrix(x, y));
            }
            outerArray.add(innerArr);
        }
        return outerArray;
    }

    public void stepAndDrawAll(ShapeRenderer sr) {
        stepAll();
        drawAll(sr);
    }

    private void stepAll() {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void drawAll(ShapeRenderer sr) {
        drawElements(sr);
//        drawChunks(sr);
    }

    private void drawElements(ShapeRenderer sr) {
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element.owningBody != null) {
                    continue;
                }
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    Element followingElement = row.get(following);
                    if (!followingElement.color.equals(currentColor)) {
                        break;
                    }
                    toIndex = following;
                }

                sr.setColor(element.color);
                sr.rect(element.toPixel(x), element.toPixel(y), rectDrawWidth(toIndex), pixelSizeModifier);
                x = toIndex;

            }
        }
        sr.end();
    }

    private void drawChunks(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(ColorConstants.getColorForElementType(ElementType.LAVA));
        for (int y = 0; y < chunks.size; y++) {
            Array<Chunk> chunkRow = chunks.get(y);
            for (int x = 0; x < chunkRow.size; x++) {
                Chunk chunk = chunkRow.get(x);
                if (chunk.getShouldStep()) {
                    sr.rect(chunk.getTopLeft().x * pixelSizeModifier, chunk.getTopLeft().y * pixelSizeModifier, Chunk.size * pixelSizeModifier, Chunk.size * pixelSizeModifier);
                }
            }
        }
        sr.end();
    }

    public void drawBox2d(ShapeRenderer sr, Array<Body> bodies) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.RED);
        int mod = CellularAutomaton.box2dSizeModifier;
        for (Body body : bodies) {
            Vector2 position = body.getPosition();
            for (Fixture fixture : body.getFixtureList()) {
                Shape.Type shapeType = fixture.getShape().getType();
                switch (shapeType) {
                    case Circle:
                        sr.set(ShapeRenderer.ShapeType.Line);
                        sr.circle(position.x * mod, position.y * mod, fixture.getShape().getRadius() * mod);
                        break;
                    case Polygon:
                        PolygonShape polygon = (PolygonShape) fixture.getShape();
                        int vertexCount = polygon.getVertexCount();

                        Vector2 previousVertex = new Vector2();

                        for (int i = 0; i < vertexCount; i++) {
                            Vector2 currentVertex = new Vector2();
                            polygon.getVertex(i, currentVertex);
                            if (i == 0) {
                                polygon.getVertex(vertexCount - 1, previousVertex);
                            } else {
                                polygon.getVertex(i - 1, previousVertex);
                            }

                            previousVertex = body.getWorldPoint(previousVertex);
                            previousVertex.x *= mod;
                            previousVertex.y *= mod;
                            Vector2 prevVertCopy = previousVertex.cpy();
                            currentVertex = body.getWorldPoint(currentVertex);
                            currentVertex.x *= mod;
                            currentVertex.y *= mod;
                            Vector2 curVertCopy = previousVertex.cpy();
                            sr.line(curVertCopy, prevVertCopy);

                        }
                        sr.circle(position.x * mod, position.y * mod, 2);
                        break;
                }
            }
        }
        sr.end();
    }

    private float rectDrawWidth(int index) {
        return (index * pixelSizeModifier) + (pixelSizeModifier);
    }

    public void stepProvidedRows(int minRow, int maxRow) {
        for (int y = minRow; y <= maxRow; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void stepProvidedColumns(int colIndex) {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : shuffledXIndexesForThreads.get(colIndex)) {
                try {
                    Element element = row.get(calculateIndexWithOffset(x));
                    if (element != null) {
                        element.step(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void drawProvidedRows(int minRow, int maxRow, ShapeRenderer sr) {
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = minRow; y < maxRow; y++) {
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element.owningBody != null) {
                    continue;
                }
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    Element followingElement = row.get(following);
                    if (!followingElement.color.equals(currentColor)) {
                        break;
                    }
                    toIndex = following;
                }

                sr.setColor(element.color);
                sr.rect(element.toPixel(x), element.toPixel(y), rectDrawWidth(toIndex), pixelSizeModifier);
                x = toIndex;

            }
        }
        sr.end();
    }

    private int calculateIndexWithOffset(int x) {
        if (x + threadedIndexOffset >= innerArraySize) {
            return (x + threadedIndexOffset) - (innerArraySize);
        } else {
            return x + threadedIndexOffset;
        }
    }

    public void addSpout(ElementType elementType, Vector3 touchPos, int brushSize, InputManager.BRUSHTYPE brushtype, boolean isParticle) {
        if (isParticle) {
            spoutArray.add(new ParticleSpout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize, brushtype, this::spawnParticleByMatrixWithBrush));
        } else {
            spoutArray.add(new ElementSpout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize, brushtype,  this::spawnElementByMatrixWithBrush));
        }

    }

    public void spawnFromSpouts() {
        for (Spout spout : spoutArray) {
            FunctionInput functionInput = spout.setFunctionInputs(new FunctionInput());
            spout.getFunction().accept(functionInput);
        }
    }

    public void addExplosion(int radius, int strength, Element sourceElement) {
        explosionArray.add(new Explosion(this, radius, strength, sourceElement));
    }

    public void addExplosion(int radius, int strength, int matrixX, int matrixY) {
        explosionArray.add(new Explosion(this, radius, strength, matrixX, matrixY));
    }

    public void executeExplosions() {
        for (Explosion explosion : explosionArray) {
            explosion.enact();
        }
        //explosionArray.clear();
    }


    public int toMatrix(float pixelVal) {
        return toMatrix((int) pixelVal);
    }

    public int toMatrix(int pixelVal) {
        return pixelVal / pixelSizeModifier;
    }

    public boolean clearAll() {
        matrix = generateMatrix();
        spoutArray.clear();
        physicsElementActors.forEach(pea -> world.destroyBody(pea.getPhysicsBody()));
        physicsElementActors.clear();
        boids.clear();
        chunks.forEach(chunkInnerArray -> chunkInnerArray.forEach(Chunk::removeAllBoids));
        return true;
    }

    public Element get(Vector3 location) {
        return get((int) location.x, (int) location.y);
    }

    public Element get(float x, float y) {
        return get((int) x, (int) y);
    }

    public Element get(int x, int y) {
        if (isWithinBounds(x, y)) {
            return matrix.get(y).get(x);
        } else {
            return null;
        }
    }

    public Array<Element> getRow(int index) {
        return matrix.get(index);
    }

    public boolean setElementAtIndex(int x, int y, Element element) {
        matrix.get(y).set(x, element);
        element.setCoordinatesByMatrix(x, y);
        return true;
    }

    public boolean setElementAtSecondLocation(int x, int y, Element element) {
        if (isWithinBounds(x, y)) {
            matrix.get(y).set(x, element);
            element.setSecondaryCoordinatesByMatrix(x, y);
            return true;
        }
        return false;
    }

    public void spawnElementByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int localBrushSize, InputManager.BRUSHTYPE brushtype) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrixWithBrush(createFunctionInput(matrixX, matrixY, elementType, localBrushSize, null, brushtype));
    }

    public void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrix(matrixX, matrixY, elementType);
    }

    public Element spawnElementByMatrix(int matrixX, int matrixY, ElementType elementType) {
        if (isWithinBounds(matrixX, matrixY)) {
            Element currentElement = get(matrixX, matrixY);
            if (currentElement.getClass() != elementType.clazz && !(currentElement instanceof PlayerMeat)) {
                get(matrixX, matrixY).die(this);
                Element newElement = elementType.createElementByMatrix(matrixX, matrixY);
                setElementAtIndex(matrixX, matrixY, newElement);
                reportToChunkActive(newElement);
                return newElement;
            }
        }
        return null;
    }
    public boolean isWithinBounds(Vector2 vec) {
        return isWithinBounds((int) vec.x, (int) vec.y);
    }

    public boolean isWithinBounds(int matrixX, int matrixY) {
        return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
    }

    public static int distanceBetweenTwoPoints(int x1, int x2, int y1, int y2) {
        return (int) Math.ceil(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public boolean isWithinXBounds(int matrixX) {
        return matrixX >= 0 && matrixX < innerArraySize;
    }

    public boolean isWithinYBounds(int matrixY) {
        return matrixY >= 0 && matrixY < outerArraySize;
    }

    public List<Integer> getShuffledXIndexes() {
        return shuffledXIndexes;
    }

    public void reshuffleXIndexes() {
        Collections.shuffle(shuffledXIndexes);
    }

    private List<Integer> generateShuffledIndexes(int size) {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    public List<List<Integer>> generateShuffledIndexesForThreads(int threadCount) {
        int colSize = innerArraySize / threadCount;// + (innerArraySize % threadCount);
        List<List<Integer>> indexList = new ArrayList<>(threadCount);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= innerArraySize; i++) {
            if (i % colSize == 0) {
                Collections.shuffle(list);
                indexList.add(list);
                list = new ArrayList<>(colSize);
            }
            list.add(i - 1);
        }
        if (!indexList.contains(list)) {
            indexList.get(indexList.size() - 1).addAll(list);
        }
        shuffledXIndexesForThreads = indexList;
        return indexList;
    }

    public void reshuffleThreadXIndexes(int numThreads) {
        if (shuffledXIndexesForThreads.size() != numThreads) {
            generateShuffledIndexesForThreads(numThreads);
            return;
        }
        shuffledXIndexesForThreads.forEach(Collections::shuffle);
    }

    public void applyHeatBetweenTwoPoints(Vector3 pos1, Vector3 pos2, int brushSize, InputManager.BRUSHTYPE brushtype) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, null, brushSize, brushtype, null, this:: applyHeatByBrush);
    }

    public void spawnElementBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize, InputManager.BRUSHTYPE brushtype) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, elementType, brushSize, brushtype, null, this::spawnElementByMatrixWithBrush);
    }

    public void spawnParticleBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize, InputManager.BRUSHTYPE brushtype) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, elementType, brushSize, brushtype, null, this::spawnParticleByMatrixWithBrush);
    }

    public void particalizeBetweenTwoPoints(Vector3 pos1, Vector3 pos2, int brushSize, InputManager.BRUSHTYPE brushtype) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, null, brushSize, brushtype, null, this::particalizeByMatrixWithBrush);
    }

    public void iterateAndApplyMethodBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize, InputManager.BRUSHTYPE brushtype, Vector3 velocity, Consumer<FunctionInput> function) {

        int matrixX1 = toMatrix((int) pos1.x);
        int matrixY1 = toMatrix((int) pos1.y);
        int matrixX2 = toMatrix((int) pos2.x);
        int matrixY2 = toMatrix((int) pos2.y);

        // If the two points are the same no need to iterate. Just run the provided function
        if (pos1.epsilonEquals(pos2)) {
            FunctionInput input = createFunctionInput(matrixX1, matrixY1, elementType, brushSize, velocity, brushtype);
            function.accept(input);
            return;
        }

        int xDiff = matrixX1 - matrixX2;
        int yDiff = matrixY1 - matrixY2;
        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

        int xModifier = xDiff < 0 ? 1 : -1;
        int yModifier = yDiff < 0 ? 1 : -1;

        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
        float slope = (min == 0 || upperBound == 0) ? 0 : ((float) (min + 1) / (upperBound + 1));

        int smallerCount;
        for (int i = 1; i <= upperBound; i++) {
            smallerCount = (int) Math.floor(i * slope);
            int yIncrease, xIncrease;
            if (xDiffIsLarger) {
                xIncrease = i;
                yIncrease = smallerCount;
            } else {
                yIncrease = i;
                xIncrease = smallerCount;
            }
            int currentY = matrixY1 + (yIncrease * yModifier);
            int currentX = matrixX1 + (xIncrease * xModifier);
            if (isWithinBounds(currentX, currentY)) {
                FunctionInput input = createFunctionInput(currentX, currentY, elementType, brushSize, velocity, brushtype);
                function.accept(input);
            }
        }
    }

    public void spawnElementByMatrixWithBrush(FunctionInput input) {//int matrixX, int matrixY, ElementType elementType, int localBrushSize) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int localBrushSize = input.getBrushSize();
        ElementType elementType = input.getElementType();
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                if (input.getBrushType().equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        spawnElementByMatrix(x, y, elementType);
                    }
                } else {
                    spawnElementByMatrix(x, y, elementType);
                }
            }
        }
    }

    public void applyHeatByBrush(FunctionInput input) {
        int localBrushSize = input.getBrushSize();
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                if (input.getBrushType().equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        Element element = get(x, y);
                        if (element != null) element.receiveHeat(this, 500);
                        reportToChunkActive(x, y);
                    }
                } else {
                    Element element = get(x, y);
                    if (element != null) element.receiveHeat(this, 500);
                    reportToChunkActive(x, y);
                }
            }
        }
    }

    public FunctionInput createFunctionInput(int matrixX, int matrixY, ElementType elementType, int brushSize, Vector3 velocity, InputManager.BRUSHTYPE brushtype) {
        return new FunctionInput(matrixX, matrixY, elementType, brushSize, velocity, brushtype);
    }

    public void spawnParticleByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int brushSize, InputManager.BRUSHTYPE brushtype) {
        spawnParticleByMatrixWithBrush(createFunctionInput(toMatrix(pixelX), toMatrix(pixelY), elementType, brushSize, null, brushtype));
    }

    public void spawnParticleByMatrixWithBrush(FunctionInput input) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = input.getBrushSize()/2;
        ElementType elementType = input.getElementType();
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                if (input.getBrushType().equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        Vector3 velocity = generateRandomVelocityWithBounds(100, 300);
                        spawnParticleByMatrix(x, y, elementType, velocity);
                    }
                } else {
                    Vector3 velocity = generateRandomVelocityWithBounds(100, 300);
                    spawnParticleByMatrix(x, y, elementType, velocity);
                }
            }
        }
    }

    public void particalizeByMatrixWithBrush(FunctionInput input) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = input.getBrushSize()/2;
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                if (input.getBrushType().equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        Vector3 velocity = generateRandomVelocityWithBounds(-300, 300);
                        particalizeByMatrix(x, y, velocity);
                    }
                } else {
                    Vector3 velocity = generateRandomVelocityWithBounds(-300, 300);
                    particalizeByMatrix(x, y, velocity);
                }
            }
        }
    }

    private void spawnParticleByMatrix(int x, int y, ElementType elementType, Vector3 velocity) {
        if (get(x, y) instanceof EmptyCell) {
            Element newElement = ElementType.createParticleByMatrix(this, x, y, velocity, elementType, ColorConstants.getColorForElementType(elementType), false);
            if (newElement != null) {
                reportToChunkActive(newElement);
            }
        }
    }

    public void particalizeByPixelWithBrush(int x, int y, int brushSize, InputManager.BRUSHTYPE brushtype) {
        int matrixX = toMatrix(x);
        int matrixY = toMatrix(y);
        particalizeByMatrixWithBrush(createFunctionInput(matrixX, matrixY, null, brushSize, new Vector3(0, -124, 0), brushtype));
    }

    public void particalizeByMatrix(int x, int y, Vector3 velocity) {
        Element element = get(x, y);
        if (element instanceof MovableSolid || element instanceof Liquid) {
            element.dieAndReplaceWithParticle(this, velocity);
        }
    }

    private Vector3 generateRandomVelocityWithBounds(int lowerX, int upperX, int lowerY, int upperY) {
        int x = ThreadLocalRandom.current().nextInt(lowerX, upperX);
        int y = ThreadLocalRandom.current().nextInt(lowerY, upperY);
        return new Vector3(x, y, 0);
    }

    public Vector3 generateRandomVelocityWithBounds(int lower, int upper) {
        return generateRandomVelocityWithBounds(lower, upper, lower, upper);
    }

    public void reportToChunkActive(Element element) {
        reportToChunkActive(element.getMatrixX(), element.getMatrixY());
    }

    public void reportToChunkActive(int x, int y) {
        if (useChunks && isWithinBounds(x, y)) {
            if (x % Chunk.size == 0) {
                Chunk chunk = getChunkForCoordinates(x - 1 , y);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (x % Chunk.size == Chunk.size - 1) {
                Chunk chunk = getChunkForCoordinates(x + 1 , y);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (y % Chunk.size == 0) {
                Chunk chunk = getChunkForCoordinates(x, y - 1);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (y % Chunk.size == Chunk.size - 1) {
                Chunk chunk = getChunkForCoordinates(x, y + 1);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            getChunkForCoordinates(x, y).setShouldStepNextFrame(true);
        }
    }

    public Chunk addBoidToChunk(Boid boid, Chunk currentChunk) {
        Chunk chunk = getChunkForCoordinates(boid.getMatrixX(), boid.getMatrixY());
        if (chunk == null || chunk == currentChunk) {
            return chunk;
        }
        chunk.addBoid(boid);
        if (currentChunk != null) {
            currentChunk.removeBoid(boid);
        }
        return chunk;
    }

    public void removeBoidFromChunk(Boid boid) {
        Chunk chunk = getChunkForCoordinates(boid.getMatrixX(), boid.getMatrixY());
        chunk.removeBoid(boid);
    }

    public boolean shouldElementInChunkStep(Element element) {
        return getChunkForElement(element).getShouldStep();
    }

    public Chunk getChunkForElement(Element element) {
        return getChunkForCoordinates(element.getMatrixX(), element.getMatrixY());
    }

    public Chunk getChunkForCoordinates(int x, int y) {
        if (isWithinBounds(x, y)) {
            int chunkY = y / Chunk.size;
            int chunkX = x / Chunk.size;
            return chunks.get(chunkY).get(chunkX);
        }
        return null;
    }

    public void resetChunks() {
        for (int r = 0; r < chunks.size; r++) {
            Array<Chunk> chunkRow = chunks.get(r);
            for (int c = 0; c < chunkRow.size; c++) {
                Chunk chunk = chunkRow.get(c);
                chunk.shiftShouldStepAndReset();
            }
        }
    }

//    public void spawnRect(Vector3 mouseDownPos, Vector3 mouseUpPos, ElementType currentlySelectedElement, BodyDef.BodyType bodyType) {
//        int mod = CellularAutomaton.box2dSizeModifier;
//        int matrixMouseDownX = toMatrix(mouseDownPos.x);
//        int matrixMouseDownY = toMatrix(mouseDownPos.y);
//        int matrixMouseUpX = toMatrix(mouseUpPos.x);
//        int matrixMouseUpY = toMatrix(mouseUpPos.y);
//        Vector3 boxCenter = new Vector3((float) (matrixMouseDownX + matrixMouseUpX) / 2, (float) (matrixMouseDownY + matrixMouseUpY) / 2, 0);
//        List<Vector2> vertices = getRectVertices(matrixMouseDownX, matrixMouseUpX, matrixMouseDownY, matrixMouseUpY);
//
//        Body body = ShapeFactory.createBoxByBodyType(boxCenter, vertices, bodyType);
//        PolygonShape shape = (PolygonShape) body.getFixtureList().get(0).getShape();
//        Vector2 point = new Vector2();
//        int minX = innerArraySize;
//        int maxX = 0;
//        int minY = innerArraySize;
//        int maxY = 0;
//        for (int i = 0; i < shape.getVertexCount(); i++) {
//            shape.getVertex(i, point);
//            Vector2 worldPoint = body.getWorldPoint(point);
//            minX = Math.min(toMatrix(worldPoint.x * mod), minX);
//            maxX = Math.max(toMatrix(worldPoint.x * mod), maxX);
//            minY = Math.min(toMatrix(worldPoint.y * mod), minY);
//            maxY = Math.max(toMatrix(worldPoint.y * mod), maxY);
//        }
//        Array<Array<Element>> elementList = new Array<>();
//        int xDistance = maxX - minX;
//        int yDistance = maxY - minY;
//        ElementType type = currentlySelectedElement;
//        for (int y = minY; y < minY + yDistance; y++) {
//            Array<Element> row = new Array<>();
//            elementList.add(row);
//            for (int x = minX; x < minX + xDistance; x++) {
//
//                Element element = spawnElementByMatrix(x, y, type);
//                row.add(element);
//            }
//        }
//        PhysicsElementActor newActor = new PhysicsElementActor(body, elementList, minX, maxY);
//        physicsElementActors.add(newActor);
//    }

    public void spawnRect(Vector3 mouseDownPos, Vector3 mouseUpPos, ElementType currentlySelectedElement, BodyDef.BodyType bodyType) {
        int mod = CellularAutomaton.box2dSizeModifier;
        int matrixMouseDownX = toMatrix(mouseDownPos.x);
        int matrixMouseDownY = toMatrix(mouseDownPos.y);
        int matrixMouseUpX = toMatrix(mouseUpPos.x);
        int matrixMouseUpY = toMatrix(mouseUpPos.y);
        Vector2 boxCenter = new Vector2((float) (matrixMouseDownX + matrixMouseUpX) / 2, (float) (matrixMouseDownY + matrixMouseUpY) / 2);
        List<Vector2> vertices = getRectVertices(matrixMouseDownX, matrixMouseUpX, matrixMouseDownY, matrixMouseUpY);

        // min max are matrix coords
        int minX = innerArraySize;
        int maxX = 0;
        int minY = outerArraySize;
        int maxY = 0;
        for (Vector2 point : vertices) {
            minX = Math.min((int) point.x, minX);
            maxX = Math.max((int) point.x, maxX);
            minY = Math.min((int) point.y, minY);
            maxY = Math.max((int) point.y, maxY);
        }
        Array<Array<Element>> elementList = new Array<>();
        int xDistance = maxX - minX;
        int yDistance = maxY - minY;
        ElementType type = currentlySelectedElement;
        for (int y = minY; y < minY + yDistance; y++) {
            Array<Element> row = new Array<>();
            elementList.add(row);
            for (int x = minX; x < minX + xDistance; x++) {
                Element element = spawnElementByMatrix(x, y, type);
                row.add(element);
            }
        }
        Body body = ShapeFactory.createPolygonFromElementArray(minX, minY, elementList, bodyType);
        if (body == null) return;
        PhysicsElementActor newActor = new PhysicsElementActor(body, elementList, minX, maxY);
        physicsElementActors.add(newActor);
    }

    private List<Vector2> getRectVertices(int minX, int maxX, int minY, int maxY) {
        List<Vector2> verts = new ArrayList<>();
        verts.add(new Vector2(minX, minY));
        verts.add(new Vector2(minX, maxY));
        verts.add(new Vector2(maxX, maxY));
        verts.add(new Vector2(maxX, minY));
        return verts;
    }

    public void stepPhysicsElementActors() {
        for (PhysicsElementActor physicsElementActor : physicsElementActors) {
            physicsElementActor.step(this);
        }
    }

    public void drawPhysicsElementActors(ShapeRenderer sr) {
        for (PhysicsElementActor physicsElementActor : physicsElementActors) {
            physicsElementActor.draw(sr);
        }
    }

    public void destroyPhysicsElementActor(PhysicsElementActor physicsElementActor) {
        this.world.destroyBody(physicsElementActor.getPhysicsBody());
        this.physicsElementActors.removeValue(physicsElementActor, true);
    }

    public Array<Boid> getBoidNeighbors(int matrixX, int matrixY) {
        return getChunkBoidNeighbors(matrixX, matrixY);
    }

    public Array<Boid> getAllBoidNeighbors(int matrixX, int matrixY) {
        Array<Boid> allBoids = new Array<>(boids);
        Array<Boid> neighbors = new Array<>();
        for (Boid boid : allBoids) {
            int distance = distanceBetweenTwoPoints(matrixX, boid.getMatrixX(), matrixY, boid.getMatrixY());
            if (distance > 0 && distance <= Boid.neighborDistance) {
                neighbors.add(boid);
            }
        }
        return neighbors;
    }

    public Array<Boid> getChunkBoidNeighbors(int matrixX, int matrixY) {
        Set<Chunk> chunks = new HashSet<>();
        Chunk currentChunk = getChunkForCoordinates(matrixX, matrixY);
        chunks.add(currentChunk);
        boolean top = false;
        boolean right = false;
        boolean bottom = false;
        boolean left = false;
        if (matrixX % Chunk.size < Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY);
            if (chunk != null) {
                chunks.add(chunk);
            }
            left = true;
        }
        if (matrixX % Chunk.size > Chunk.size - Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY);
            if (chunk != null) {
                chunks.add(chunk);
            }
            right = true;
        }
        if (matrixY % Chunk.size < Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
            top = true;
        }
        if (matrixY % Chunk.size > Chunk.size - Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
            bottom = true;
        }
        if (top && right) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (right && bottom) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (bottom && left) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (top && left) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        List<Boid> allNeighbors = new ArrayList<>();
        chunks.forEach(chunk -> allNeighbors.addAll(chunk.getAllBoids()));
        List<Boid> filteredNeighbors = allNeighbors.stream().filter(boid -> {
            int distance = distanceBetweenTwoPoints(matrixX, boid.getMatrixX(), matrixY, boid.getMatrixY());
            return distance > 0 && distance < Boid.neighborDistance;
        }).collect(Collectors.toList());
        List<Boid> subList = filteredNeighbors.subList(0, Math.min(Boid.maxNeighbors, filteredNeighbors.size()));
        Array<Boid> returnList = new Array<>();
        subList.forEach(returnList::add);
        return returnList;
    }

    public void addBoid(Boid boid) {
        this.boids.add(boid);
    }

    public void spawnBoidsWithBrush(int matrixX, int matrixY, int brushSize, InputManager.BRUSHTYPE brushType) {
        int halfBrush = brushSize/2;
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                if (brushType.equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        Vector3 velocity = generateRandomVelocityWithBounds(-50, 50);
                        spawnBoid(x, y, velocity);
                    }
                } else {
                    Vector3 velocity = generateRandomVelocityWithBounds(-50, 50);
                    spawnBoid(x, y, velocity);
                }
            }
        }
    }

    public void spawnBoid(int x, int y, Vector3 velocity) {
        ElementType.createBoidByMatrix(this, x, y, velocity);
    }

    public static class FunctionInput {

        Map<String, Object> inputs = new HashMap<>();

        public static final String X = "x";
        public static final String Y = "y";
        public static final String ELEMENT_TYPE = "elementType";
        public static final String BRUSH_SIZE = "brushSize";
        public static final String BRUSH_TYPE = "brushType";
        public static final String VELOCITY = "velocity";

        public FunctionInput() {

        }

        public FunctionInput(int matrixX, int matrixY, int brushSize) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(BRUSH_TYPE, InputManager.BRUSHTYPE.CIRCLE);
        }

        public FunctionInput(int matrixX, int matrixY, int brushSize, InputManager.BRUSHTYPE brushType) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(BRUSH_TYPE, brushType);
        }

        public FunctionInput(int matrixX, int matrixY, int brushSize, ElementType elementType) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(ELEMENT_TYPE, elementType);
        }

        public FunctionInput(int matrixX, int matrixY, ElementType elementType, int brushSize, Vector3 velocity, InputManager.BRUSHTYPE brushType) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(ELEMENT_TYPE, elementType);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(BRUSH_TYPE, brushType);
            inputs.put(VELOCITY, velocity);
        }

        public void setInput(String key, Object value) {
            inputs.put(key, value);
        }

        public int getMatrixX() {
            return (int) inputs.get(X);
        }

        public int getMatrixY() {
            return (int) inputs.get(Y);
        }

        public ElementType getElementType() {
            return (ElementType) inputs.get(ELEMENT_TYPE);
        }

        public int getBrushSize() {
            return (int) inputs.get(BRUSH_SIZE);
        }

        public InputManager.BRUSHTYPE getBrushType() { return (InputManager.BRUSHTYPE) inputs.get(BRUSH_TYPE); }

        public Vector3 getVelocity() {
            return (Vector3) inputs.get(VELOCITY);
        }
    }

}
