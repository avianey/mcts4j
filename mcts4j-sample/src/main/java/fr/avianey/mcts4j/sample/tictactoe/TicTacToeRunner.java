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

import fr.avianey.mcts4j.sample.SampleRunner;

/**
 * Run a game between two TicTacToeIA opponent...
 *
 * @author antoine vianey
 */
public class TicTacToeRunner extends SampleRunner<TicTacToeTransition> {

    public TicTacToeRunner() {
        // Change the thinking depth value > 0
        super(new TicTacToeIA());
    }

    public static void main(String[] args) {
        SampleRunner<TicTacToeTransition> runner = new TicTacToeRunner();
        runner.run();
    }

}
