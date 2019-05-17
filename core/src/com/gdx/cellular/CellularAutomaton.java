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
import com.gdx.cellular.elements.ElementType;

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
    private OrthographicCamera camera;
    private boolean touchedLastFrame;
    private ElementType currentlySelectedElement = ElementType.SAND;
    private Vector3 lastTouchPos = new Vector3();
    private int brushSize = 3;

    private int numThreads = 12;
    private int maxThreads = 50;
    private boolean toggleThreads = false;

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
		matrix.generateShuffledIndexesForThreads(numThreads);
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
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
			toggleThreads = !toggleThreads;
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			numThreads += numThreads == maxThreads ? 0 : 1;
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			numThreads -= numThreads == 1 ? 0 : 1;
		}

		matrix.reshuffleXIndexes();
		matrix.reshuffleThreadXIndexes(numThreads);
		matrix.calculateAndSetThreadedXIndexOffset();


		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			if (touchedLastFrame) {
			    iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
            } else {
                matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
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
			matrix.addSpout(currentlySelectedElement, touchPos, brushSize);
		}

		matrix.spawnFromSpouts();

		if (toggleThreads) {
			matrix.stepAndDrawAll(shapeRenderer);
		} else {
			matrix.reshuffleThreadXIndexes(numThreads);
			List<Thread> threads = new ArrayList<>(numThreads);

			for (int t = 0; t < numThreads; t++) {
				Thread newThread = new Thread(new ElementColumnStepper(matrix, t));
				threads.add(newThread);
			}
			if (stepped.get(0)) {
				startAndWaitOnOddThreads(threads);
				startAndWaitOnEvenThreads(threads);
			} else {
				startAndWaitOnEvenThreads(threads);
				startAndWaitOnOddThreads(threads);
			}


			matrix.drawAll(shapeRenderer);
		}

	}

	private void startAndWaitOnEvenThreads(List<Thread> threads) {
		try {
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 == 0) {
					threads.get(t).start();
				}
			}
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 == 0) {
					threads.get(t).join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startAndWaitOnOddThreads(List<Thread> threads) {
		try {
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 != 0) {
					threads.get(t).start();
				}
			}
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 != 0) {
					threads.get(t).join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void iterateAndSpawnBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize) {

		int matrixX1 = toMatrix((int) pos1.x);
		int matrixY1 = toMatrix((int) pos1.y);
		int matrixX2 = toMatrix((int) pos2.x);
		int matrixY2 = toMatrix((int) pos2.y);

		if (pos1.epsilonEquals(pos2)) {
			matrix.spawnElementByMatrixWithBrush(matrixX1, matrixY1, elementType, brushSize);
			return;
		}

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
	        	matrix.spawnElementByMatrixWithBrush(currentX, currentY, elementType, brushSize);
			}
        }


    }

    public static int toMatrix(int pixelVal) {
	    return pixelVal / pixelSizeModifier;
    }

    @Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
	}

}
