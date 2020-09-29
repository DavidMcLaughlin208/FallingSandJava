package com.gdx.cellular.util;

import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.player.Player;

public class GameManager {

    public CellularAutomaton cellularAutomaton;
    public final int maxPlayers = 4;
    Array<Player> players = new Array<>();

    public GameManager(CellularAutomaton cellularAutomaton) {
        this.cellularAutomaton = cellularAutomaton;
        for (int i = 0; i < maxPlayers; i++) {
            players.add(null);
        }
    }

    public void stepPlayers(CellularMatrix matrix) {
        for (int i = 0; i < players.size; i++) {
            Player player = players.get(i);
            if (player == null) {
                continue;
            }
            player.step(matrix);
        }
    }

    public Player createPlayer(int x, int y) {
        int index = -1;
        for (int i = 0; i < players.size; i++) {
            Player player = players.get(i);
            if (player == null) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return null;
        }
        Player newPlayer = new Player(x, y, index, cellularAutomaton.matrix);
        players.set(index, newPlayer);
        return newPlayer;
    }

    public void deletePlayer(int playerIndex) {
        this.players.get(playerIndex).delete(cellularAutomaton.matrix);
        this.players.set(playerIndex, null);
    }


}
