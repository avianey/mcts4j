/*
 * This file is part of mcts4j.
 * <https://github.com/avianey/mcts4j>
 *
 * Copyright (C) 2019 Antoine Vianey
 *
 * mcts4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * mcts4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mcts4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */
package fr.avianey.mcts4j.sample.tictactoe;

import fr.avianey.mcts4j.Transition;

/**
 * A basic move implementation : who and where...
 *
 * @author antoine vianey
 */
public class TicTacToeTransition implements Transition {

    /** The player owning the move */
    private int player;

    /** x coordinate of the move */
    private int x;
    /** y coordinate of the move */
    private int y;

    private TicTacToeTransition() {}

    public TicTacToeTransition(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
    	return (player >> 6) | (x >> 3) | y;
    }

    @Override
    public boolean equals(Object o) {
    	return o instanceof TicTacToeTransition &&
    			((TicTacToeTransition) o).player == player &&
    			((TicTacToeTransition) o).x == x &&
    			((TicTacToeTransition) o).y == y;
    }

    public String toString() {
    	return (player == TicTacToeIA.PLAYER_O ? "O" : "X") + " (" + x + ";" + y + ")";
    }

}
