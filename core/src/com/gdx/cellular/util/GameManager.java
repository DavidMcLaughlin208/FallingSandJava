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

    public Player createPlayer(int x, int y) {
        int index = this.players.indexOf(null, false);
        if (index == -1) {
            return null;
        }
        Player newPlayer = new Player(x, y, index, cellularAutomaton.matrix);
        players.add(newPlayer);
        return newPlayer;
    }

    public void deletePlayer(int playerIndex) {
        this.players.get(playerIndex).delete(cellularAutomaton.matrix);
        this.players.set(playerIndex, null);
    }


}
