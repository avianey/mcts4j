package fr.avianey.mcts4j;

import static java.lang.Math.log;
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
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mcts4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */
import static java.lang.Math.sqrt;

public abstract class UCT<T extends Transition> extends MonteCarloTreeSearch<T> {

    private static final double C = sqrt(2);

    // TODO if node is leaf pick random transition
    @Override
    @SuppressWarnings("unchecked")
    public T selectTransition(Node<T> node, final int player) {
        double v = Double.NEGATIVE_INFINITY;
        T best = null;
        for (T transition : getPossibleTransitions()) {
            Node<T> n = node.getChild(transition);
            if (n == null) {
                // unexplored path
                return transition;
            }
            if (!n.isTerminal()) {
                // child already explored and non terminal
                long simulations = n.simulations();
                assert simulations > 0;
                long wins = n.wins(player);
                // w/n + C * Math.sqrt(ln(n(p)) / n)
                // TODO : add a random hint to avoid ex-aequo
                double value = (simulations == 0 ? 0 : wins / simulations + C * sqrt(log(node.simulations()) / simulations));
                if (value > v) {
                    v = value;
                    best = transition;
                }
            }
        }
        return best;
    }

}
