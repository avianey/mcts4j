package fr.avianey.mcts4j.sample.tictactoe;

import fr.avianey.mcts4j.Transition;

/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012 Antoine Vianey
 * 
 * minimax4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with minimax4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */

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
