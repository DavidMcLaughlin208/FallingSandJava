package com.gdx.cellular.input.processors;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.gdx.cellular.input.InputProcessors;
import com.gdx.cellular.player.Player;
import com.gdx.cellular.util.GameManager;

public class PlayerInputProcessor implements InputProcessor {

    private GameManager gameManager;
    private InputProcessors parent;

    public PlayerInputProcessor(InputProcessors inputProcessors, GameManager gameManager) {
        this.parent = inputProcessors;
        this.gameManager = gameManager;
    }



    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            this.parent.setCreatorInputProcessor();
            return true;
        }
        Player player1 = gameManager.getPlayer(0);
        if (keycode == Input.Keys.A) {
            player1.setXVelocity(-62);
        } else if (keycode == Input.Keys.D) {
            player1.setXVelocity(62);
        } else if (keycode == Input.Keys.W) {
            player1.setYVelocity(250);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Player player1 = gameManager.getPlayer(0);
        if (keycode == Input.Keys.A) {
            player1.setXVelocity(0);
        } else if (keycode == Input.Keys.D) {
            player1.setXVelocity(0);
        } else if (keycode == Input.Keys.W) {
            player1.setYVelocity(0);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
