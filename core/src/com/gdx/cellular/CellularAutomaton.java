package com.gdx.cellular;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.input.InputManager;
import com.gdx.cellular.input.InputProcessors;
import com.gdx.cellular.ui.MatrixActor;
import com.gdx.cellular.util.ElementColumnStepper;
import com.gdx.cellular.util.GameManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class CellularAutomaton extends ApplicationAdapter {
	public static int screenWidth = 1280; // 480;
	public static int screenHeight = 800; //800;
	public static int pixelSizeModifier = 8;
	public static int box2dSizeModifier = 10;
    public static Vector3 gravity = new Vector3(0f, -5f, 0f);
    public static BitSet stepped = new BitSet(1);

    private ShapeRenderer shapeRenderer;
    public CellularMatrix matrix;
    private OrthographicCamera camera;

    private int numThreads = 12;
    private boolean useMultiThreading = true;

    private InputManager inputManager;

	private FPSLogger fpsLogger;
	public static int frameCount = 0;
	public boolean useChunks = true;
	public World b2dWorld;
	public Box2DDebugRenderer debugRenderer;
	public InputProcessors inputProcessors;
	public Stage matrixStage;
	public GameManager gameManager;

	@Override
	public void create () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		fpsLogger = new FPSLogger();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);
		camera.zoom = 1f;

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setAutoShapeType(true);

        stepped.set(0, true);

		Viewport viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
		inputManager = new InputManager(camera, viewport, shapeRenderer);

		b2dWorld = new World(new Vector2(0, -100), true);

		matrix = new CellularMatrix(screenWidth, screenHeight, pixelSizeModifier, b2dWorld);
		matrix.generateShuffledIndexesForThreads(numThreads);

		matrixStage = new Stage(viewport);
		matrixStage.addActor(new MatrixActor(shapeRenderer, matrix));

		ShapeFactory.initialize(b2dWorld);
		debugRenderer = new Box2DDebugRenderer();

		setUpBasicBodies();

		this.gameManager = new GameManager(this);
		gameManager.createPlayer(matrix.innerArraySize/2, matrix.outerArraySize/2);
		inputProcessors = new InputProcessors(inputManager, matrix, camera, gameManager);
	}

	@Override
	public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fpsLogger.log();
        stepped.flip(0);
        incrementFrameCount();

        if (useChunks) {
			matrix.resetChunks();
		}

        // Detect and act on input
        numThreads = inputManager.adjustThreadCount(numThreads);
        useMultiThreading = inputManager.toggleThreads(useMultiThreading);
        useChunks = inputManager.toggleChunks(useChunks);
		inputManager.save(matrix);
		inputManager.load(matrix);

		matrix.reshuffleXIndexes();
		matrix.reshuffleThreadXIndexes(numThreads);
		matrix.calculateAndSetThreadedXIndexOffset();

		boolean isPaused = inputManager.getIsPaused();
		if (isPaused) {
			matrix.useChunks = false;
			useChunks = false;
			matrixStage.draw();
			matrix.drawPhysicsElementActors(shapeRenderer);
			Array<Body> bodies = new Array<>();
			b2dWorld.getBodies(bodies);
			matrix.drawBox2d(shapeRenderer, bodies);
			debugRenderer.render(b2dWorld, camera.combined);
			return;
		}

		matrix.spawnFromSpouts();
		matrix.useChunks = useChunks;

		if (!useMultiThreading) {
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
//			matrix.drawAll(shapeRenderer);

		}

		matrix.executeExplosions();

		b2dWorld.step(1/120f, 10, 6);
		b2dWorld.step(1/120f, 10, 6);
		matrix.stepPhysicsElementActors();

		matrixStage.draw();
		matrix.drawPhysicsElementActors(shapeRenderer);

		Array<Body> bodies = new Array<>();
		b2dWorld.getBodies(bodies);
		matrix.drawBox2d(shapeRenderer, bodies);
		debugRenderer.render(b2dWorld, camera.combined);

		inputManager.drawMenu();
		inputManager.drawCursor();

		inputManager.weatherSystem.enact(this.matrix);
		gameManager.stepPlayers(this.matrix);
//		try {
//			Thread.sleep(2);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void resize (int width, int height) {
		matrixStage.getViewport().update(width, height, true);
		inputManager.cursorStage.getViewport().update(width, height, true);
		inputManager.modeStage.getViewport().update(width, height, true);
	}

	private void incrementFrameCount() {
		frameCount = frameCount == 3 ? 0 : frameCount + 1;
	}

	private void setUpBasicBodies() {
		BodyDef groundBodyDef = new BodyDef();

		inputManager.spawnPhysicsRect(matrix, new Vector3((camera.viewportWidth/2/box2dSizeModifier/8) * 10, 150, 0),
				new Vector3((camera.viewportWidth/2/box2dSizeModifier - camera.viewportWidth/2/box2dSizeModifier/8) * 20, 50, 0),
				ElementType.STONE,
				BodyDef.BodyType.StaticBody);
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
				if (t % 2 == 0) {
					threads.get(t).join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    @Override
	public void dispose () {
		shapeRenderer.dispose();
	}

}
