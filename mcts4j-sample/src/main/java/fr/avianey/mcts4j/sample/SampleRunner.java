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
package fr.avianey.mcts4j.sample;

import fr.avianey.mcts4j.MonteCarloTreeSearch;
import fr.avianey.mcts4j.Transition;

import java.util.Set;

/**
 * An abstract utility class for testing MCTS implementations.
 *
 * @author antoine vianey
 *
 * @param <T>
 */
public abstract class SampleRunner<T extends Transition> {

    public interface Listener<T extends Transition> {
        void onMove(MonteCarloTreeSearch<T> mcts, T transition, int turn);
        void onGameOver(MonteCarloTreeSearch<T> mcts);
        void onNoPossibleMove(MonteCarloTreeSearch<T> mcts);
    }

    private MonteCarloTreeSearch<T> mcts;
    private Listener<T> listener;

    public SampleRunner(MonteCarloTreeSearch<T> mcts) {
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
            }
        }
        if (listener != null) {
            listener.onGameOver(mcts);
        }
    }

}
