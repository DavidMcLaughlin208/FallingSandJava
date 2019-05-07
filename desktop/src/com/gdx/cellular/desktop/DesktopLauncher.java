package com.gdx.cellular.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gdx.cellular.CellularAutomaton;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//        config.vSyncEnabled = false;
//		config.foregroundFPS = 0; // Setting to 0 disables foreground fps throttling
//		config.backgroundFPS = 0;
		config.width = 1000; // 480;
		config.height = 1000; //800;
		new LwjglApplication(new CellularAutomaton(), config);
	}
}
