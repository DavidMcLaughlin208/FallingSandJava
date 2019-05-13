package com.gdx.cellular;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.MovableSolid;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class CellularAutomaton extends ApplicationAdapter {
	public static int screenWidth = 800; // 480;
	public static int screenHeight = 800; //800;
	public static int pixelSizeModifier = 2;
    public static Vector3 gravity = new Vector3(0f, -5f, 0f);
    public static BitSet stepped = new BitSet(1);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private CellularMatrix matrix;
    private Array<Spout> spoutArray;
    private OrthographicCamera camera;
    private boolean touchedLastFrame;
    public int outerArraySize;
    public int innerArraySize;
    private ElementType currentlySelectedElement = ElementType.SAND;
    private Vector3 lastTouchPos = new Vector3();
    private int brushSize = 3;

    private boolean paused = false;

	private FPSLogger fpsLogger;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setAutoShapeType(true);

        stepped.set(0, true);
		matrix = new CellularMatrix(screenWidth, screenHeight, pixelSizeModifier);
		spoutArray = new Array<>();
	}

	@Override
	public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        boolean stepOneFrame = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            stepOneFrame = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
        }
        if (paused && !stepOneFrame) {
            matrix.stepAndDrawAll(shapeRenderer);
            return;
        }
        fpsLogger.log();
        stepped.flip(0);
		matrix.reshuffleXIndexes();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            currentlySelectedElement = ElementType.STONE;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            currentlySelectedElement = ElementType.SAND;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            currentlySelectedElement = ElementType.DIRT;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            currentlySelectedElement = ElementType.WATER;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            currentlySelectedElement = ElementType.OIL;
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
			currentlySelectedElement = ElementType.EMPTY_CELL;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
			brushSize = Math.min(55, brushSize + 2);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
			brushSize = Math.max(1, brushSize - 2);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			matrix.clearAll();
		}


		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			if (touchedLastFrame) {
			    iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
            } else {
                spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
            }
			lastTouchPos = touchPos;
			touchedLastFrame = true;
		} else {
			touchedLastFrame = false;
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			spoutArray.add(new Spout(currentlySelectedElement, matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize));
		}

		for (Spout spout : spoutArray) {
			spawnElementByMatrixWithBrush(spout.matrixX, spout.matrixY, spout.sourceElement, spout.brushSize);
		}

        matrix.stepAndDrawAll(shapeRenderer);

		//drawAll();
	}

	private void stepAll() {

    }

	private void drawAll() {

    }

	private void iterateAndSpawnBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize) {
	    if (pos1.epsilonEquals(pos2)) return;

		int matrixX1 = toMatrix((int) pos1.x);
		int matrixY1 = toMatrix((int) pos1.y);
		int matrixX2 = toMatrix((int) pos2.x);
		int matrixY2 = toMatrix((int) pos2.y);

		int xDiff = matrixX1 - matrixX2;
		int yDiff = matrixY1 - matrixY2;
		boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

		int xModifier = xDiff < 0 ? 1 : -1;
		int yModifier = yDiff < 0 ? 1 : -1;

		int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
		int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
		int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

	    int smallerCount = 0;
	    for (int i = 1; i <= upperBound; i++) {
	        if (freq != 0 && i % freq == 0 && min != smallerCount) {
				smallerCount += 1;
            }
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
	        if (matrix.isWithinBounds(currentX, currentY)) {
	        	spawnElementByMatrixWithBrush(currentX, currentY, elementType, brushSize);
			}
        }


    }

    public static int toMatrix(int pixelVal) {
	    return pixelVal / pixelSizeModifier;
    }

	private void spawnElementByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int localBrushSize) {
		int matrixX = toMatrix(pixelX);
		int matrixY = toMatrix(pixelY);
		spawnElementByMatrixWithBrush(matrixX, matrixY, elementType, localBrushSize);
	}

    private void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
		spawnElementByMatrix(matrixX, matrixY, elementType);
	}

	private void spawnElementByMatrixWithBrush(int matrixX, int matrixY, ElementType elementType, int localBrushSize) {
		int halfBrush = (int) Math.floor(localBrushSize / 2);
		for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
			for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
				spawnElementByMatrix(x, y, elementType);
			}
		}
	}

	private void spawnElementByMatrix(int matrixX, int matrixY, ElementType elementType) {
		if (matrix.isWithinBounds(matrixX, matrixY) && matrix.get(matrixX, matrixY).getClass() != elementType.clazz) {
			matrix.setElementAtIndex(matrixX, matrixY, elementType.createElementByMatrix(matrixX, matrixY));
		}
	}

//    private int adjustPixelValueToFitGrid(float val) {
//        int adjustedVal = (int) val;
//        while (adjustedVal % pixelSizeModifier != 0) {
//            adjustedVal -= 1;
//        }
//        return adjustedVal;
//    }

//    private boolean isWithinBounds(int matrixX, int matrixY) {
//	    return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
//    }

    @Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
	}

//	private Array<Array<Element>> generateMatrix() {
//		Array<Array<Element>> outerArray = new Array<>(true, outerArraySize);
//		for (int y = 0; y < outerArraySize; y++) {
//			Array<Element> innerArr = new Array<>(true, innerArraySize);
//			for (int x = 0; x < innerArraySize; x++) {
//				innerArr.add(ElementType.EMPTY_CELL.createElementByMatrix(x, y));
//			}
//			outerArray.add(innerArr);
//		}
//		return outerArray;
//	}

//	private List<Integer> generateShuffledIndexes() {
//		List<Integer> list = new ArrayList<>(innerArraySize);
//		for (int i = 0; i < innerArraySize; i++) {
//			list.add(i);
//		}
//		return list;
//	}

//    private Vector3 multiplyVectorByConstant(Vector3 gravity, float deltaTime) {
//        return new Vector3(gravity.x * deltaTime, gravity.y * deltaTime, gravity.z * deltaTime);
//    }
}
