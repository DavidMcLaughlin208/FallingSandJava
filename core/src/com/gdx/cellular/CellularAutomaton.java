package com.gdx.cellular;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;

import java.util.BitSet;


public class CellularAutomaton extends ApplicationAdapter {
	public static int screenWidth = 480;
	public static int screenHeight = 800;
	public static int pixelSizeModifier = 3;
    public static Vector3 gravity = new Vector3(0f, -5f, 0f);
    public static BitSet stepped = new BitSet(1);

    private SpriteBatch batch;
    //	private Texture img;
    private ShapeRenderer shapeRenderer;
    private Array<Array<Element>> matrix;
    private OrthographicCamera camera;
    private boolean touchedLastFrame;
    public int outerArraySize;
    public int innerArraySize;
    private ElementType currentlySelectedElement = ElementType.SAND;
    private Vector3 lastTouchPos = new Vector3();

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
		matrix = generateMatrix();
	}

	@Override
	public void render () {
		fpsLogger.log();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
        stepped.flip(0);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            currentlySelectedElement = ElementType.STONE;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            currentlySelectedElement = ElementType.SAND;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            currentlySelectedElement = ElementType.EMPTY_CELL;
        }

		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			if (touchedLastFrame) {
			    iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement);
            } else {
                spawnElementByPixel((int) touchPos.x, (int) touchPos.y, currentlySelectedElement);
            }
			lastTouchPos = touchPos;
			touchedLastFrame = true;
		} else {
			touchedLastFrame = false;
		}

        for (int y = 0; y < matrix.size; y++) {
            Array<Element> row = matrix.get(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(matrix);
                }
            }
        }

		shapeRenderer.begin();
		shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < matrix.size; y++) {
			Array<Element> row = matrix.get(y);
			for (int x = 0; x < row.size; x++) {
				Element element = row.get(x);
				if (element != null) {
					element.draw(shapeRenderer);
				}
			}
		}
		shapeRenderer.end();
	}

    private void iterateAndSpawnBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType) {
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
	        if (isWithinBounds(currentX, currentY)) {
				matrix.get(currentY).set(currentX, elementType.createElementByMatrix(currentX, currentY));
			}
        }


    }

    public static int toMatrix(int pixelVal) {
	    return pixelVal / pixelSizeModifier;
    }

    private void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
		if (isWithinBounds(matrixX, matrixY)) {
            matrix.get(matrixY).set(matrixX, elementType.createElementByMatrix(matrixX, matrixY));
        }
	}

    private int adjustPixelValueToFitGrid(float val) {
        int adjustedVal = (int) val;
        while (adjustedVal % pixelSizeModifier != 0) {
            adjustedVal -= 1;
        }
        return adjustedVal;
    }

    private boolean isWithinBounds(int matrixX, int matrixY) {
	    return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
    }

    @Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
//		img.dispose();
	}

	private Array<Array<Element>> generateMatrix() {
	    outerArraySize = toMatrix(screenHeight);
		innerArraySize = toMatrix(screenWidth);
		Array<Array<Element>> outerArray = new Array<>(true, outerArraySize);
		for (int y = 0; y < outerArraySize; y++) {
			Array<Element> innerArr = new Array<>(true, innerArraySize);
			for (int x = 0; x < innerArraySize; x++) {
				innerArr.add(ElementType.EMPTY_CELL.createElementByMatrix(x, y));
			}
			outerArray.add(innerArr);
		}
		return outerArray;
	}

    private Vector3 multiplyVectorByConstant(Vector3 gravity, float deltaTime) {
        return new Vector3(gravity.x * deltaTime, gravity.y * deltaTime, gravity.z * deltaTime);
    }
}
