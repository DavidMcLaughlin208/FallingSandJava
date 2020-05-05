package com.gdx.cellular;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.ElementType;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class CellularAutomaton extends ApplicationAdapter {
	public static int screenWidth = 1280; // 480;
	public static int screenHeight = 800; //800;
	public static int pixelSizeModifier = 2;
    public static Vector3 gravity = new Vector3(0f, -5f, 0f);
    public static BitSet stepped = new BitSet(1);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Pixmap pixmap;
    private CellularMatrix matrix;
    private OrthographicCamera camera;

    private ElementType currentlySelectedElement = ElementType.SAND;

    private int brushSize = 5;

    private int numThreads = 12;
    private boolean useMultiThreading = false;

    private InputManager inputManager;

	private FPSLogger fpsLogger;
	public static int frameCount = 0;

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setAutoShapeType(true);

		pixmap = new Pixmap(0,0, Pixmap.Format.fromGdx2DPixmapFormat(1));

        stepped.set(0, true);
		matrix = new CellularMatrix(screenWidth, screenHeight, pixelSizeModifier);
		matrix.generateShuffledIndexesForThreads(numThreads);

		inputManager = new InputManager();
	}

	@Override
	public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean isPaused = inputManager.getIsPaused();
        if (isPaused) {
            matrix.drawAll(shapeRenderer);
            return;
        }
        fpsLogger.log();
        stepped.flip(0);
        incrementFrameCount();

        // Detect and act on input
        currentlySelectedElement = inputManager.getNewlySelectedElementWithDefault(currentlySelectedElement);
        brushSize = inputManager.calculateNewBrushSize(brushSize);
        numThreads = inputManager.adjustThreadCount(numThreads);
        useMultiThreading = inputManager.toggleThreads(useMultiThreading);
        inputManager.cycleMouseModes();
		inputManager.clearMatrixIfInput(matrix);
		inputManager.placeSpout(matrix, camera, currentlySelectedElement, brushSize);
		inputManager.spawnElementByInput(matrix, camera, currentlySelectedElement, brushSize);

		matrix.reshuffleXIndexes();
		matrix.reshuffleThreadXIndexes(numThreads);
		matrix.calculateAndSetThreadedXIndexOffset();

		matrix.spawnFromSpouts();

		if (useMultiThreading) {
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

	private void incrementFrameCount() {
		frameCount = frameCount == 3 ? 0 : frameCount + 1;
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

    @Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
	}

}
