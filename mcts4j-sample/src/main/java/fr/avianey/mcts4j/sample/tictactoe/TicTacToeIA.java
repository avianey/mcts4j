package fr.avianey.mcts4j.sample.tictactoe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.avianey.mcts4j.DefaultNode;
import fr.avianey.mcts4j.Node;
import fr.avianey.mcts4j.UCT;

/*
 * This file is part of mcts4j.
 * <https://github.com/avianey/mcts4j>
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
 * Simple TicTacToe IA to showcase the API. 
 * 
 * @author antoine vianey
 */
public class TicTacToeIA extends UCT<TicTacToeTransition, DefaultNode<TicTacToeTransition>> {

    static final int FREE       = 0;
    static final int PLAYER_X   = 1; // X
    static final int PLAYER_O   = 2; // O
    
    private static final int GRID_SIZE  = 3;
    
    /** The grid */
    private final int[][] grid;
    
    private int currentPlayer;
    private int turn = 0;

    public TicTacToeIA() {
    	super();
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        newGame();
    }
    
    public void newGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = FREE;
            }
        }
        // X start to play
        currentPlayer = PLAYER_X;
        turn = 0;
    }

    @Override
    public boolean isOver() {
        return hasWon(PLAYER_O) || hasWon(PLAYER_X) || turn == 9;
    }
    
    private boolean hasWon(int player) {
        return 
            (player == grid[0][1] && player == grid[0][2] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[1][2] && player == grid[1][0])
            ||
            (player == grid[2][1] && player == grid[2][2] && player == grid[2][0])
            ||
            (player == grid[1][0] && player == grid[2][0] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[2][1] && player == grid[0][1])
            ||
            (player == grid[1][2] && player == grid[2][2] && player == grid[0][2])
            ||
            (player == grid[1][1] && player == grid[2][2] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[2][0] && player == grid[0][2]);
    }

    @Override
    public void makeTransition(TicTacToeTransition transition) {
        grid[transition.getX()][transition.getY()] = currentPlayer;
        turn++;
        next();
    }

    @Override
    public void unmakeTransition(TicTacToeTransition transition) {
        grid[transition.getX()][transition.getY()] = FREE;
        turn--;
        previous();
    }

    @Override
    public Set<TicTacToeTransition> getPossibleTransitions() {
    	Set<TicTacToeTransition> moves = new HashSet<TicTacToeTransition>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == FREE) {
                    moves.add(new TicTacToeTransition(i, j, currentPlayer));
                }
            }
        }
        // moves can be sorted to optimize alpha-beta pruning
        // {1,1} is always the best move when available
        return moves;
    }

    @Override
    public void next() {
        currentPlayer = 3 - currentPlayer;
    }

    @Override
    public void previous() {
        currentPlayer = 3 - currentPlayer;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(grid[0][0] == FREE ? " " : (grid[0][0] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][0] == FREE ? " " : (grid[1][0] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][0] == FREE ? " " : (grid[2][0] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        sb.append(grid[0][1] == FREE ? " " : (grid[0][1] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][1] == FREE ? " " : (grid[1][1] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][1] == FREE ? " " : (grid[2][1] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        sb.append(grid[0][2] == FREE ? " " : (grid[0][2] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][2] == FREE ? " " : (grid[1][2] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][2] == FREE ? " " : (grid[2][2] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        return sb.toString();
    }

	@Override
	public TicTacToeTransition simulationTransition(Set<TicTacToeTransition> possibleTransitions) {
		List<TicTacToeTransition> transitions = new ArrayList<TicTacToeTransition>(possibleTransitions);
		return transitions.get((int) Math.floor(Math.random() * possibleTransitions.size()));
	}

	@Override
	public TicTacToeTransition expansionTransition(Set<TicTacToeTransition> possibleTransitions) {
		List<TicTacToeTransition> transitions = new ArrayList<TicTacToeTransition>(possibleTransitions);
		return transitions.get((int) Math.floor(Math.random() * possibleTransitions.size()));
	}

	@Override
	public int getWinner() {
		// TODO : handle draw with null return ?
		if (hasWon(PLAYER_O)) {
			return PLAYER_O;
		} else {
			return PLAYER_X;
		}
	}

	@Override
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	@Override
	public DefaultNode<TicTacToeTransition> newNode(Node<TicTacToeTransition> parent, boolean terminal) {
		return new DefaultNode<TicTacToeTransition>(parent, terminal);
	}

}
