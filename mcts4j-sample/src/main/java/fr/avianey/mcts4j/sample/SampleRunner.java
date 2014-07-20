package fr.avianey.mcts4j.sample;

import java.util.Set;

import fr.avianey.mcts4j.MonteCarloTreeSearch;
import fr.avianey.mcts4j.Node;
import fr.avianey.mcts4j.Transition;

/*
 * This file is part of mcts4j.
 * <https://github.com/avianey/mcts4j>
 *  
 * Copyright (C) 2014 Antoine Vianey
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
 * An abstract utility class for testing MCTS implementations.
 * 
 * @author antoine vianey
 *
 * @param <T>
 */
public abstract class SampleRunner<T extends Transition> {
    
    public static interface Listener<T extends Transition> {
        public void onMove(MonteCarloTreeSearch<T, ? extends Node<T>> mcts, T transition, int turn);
        public void onGameOver(MonteCarloTreeSearch<T, ? extends Node<T>> mcts);
        public void onNoPossibleMove(MonteCarloTreeSearch<T, ? extends Node<T>> mcts);
    }

    private MonteCarloTreeSearch<T, ? extends Node<T>> mcts;
    private Listener<T> listener;

    public SampleRunner(MonteCarloTreeSearch<T, ? extends Node<T>> mcts) {
        this.mcts = mcts;
    }
    
    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }
    
    public void run() {
        T transition;
        int turn = 0;
        while (!mcts.isOver()) {
            Set<T> transitions = mcts.getPossibleTransitions();
            if (!transitions.isEmpty()) {
                transition = mcts.getBestTransition();
                mcts.doTransition(transition);
                if (listener != null) {
                    listener.onMove(mcts, transition, ++turn);
                }
            } else {
                if (listener != null) {
                    listener.onNoPossibleMove(mcts);
                }
                // no move for the current player
                // up to next player
                mcts.next();
            }
        }
        if (listener != null) {
            listener.onGameOver(mcts);
        }
    }
    
}
