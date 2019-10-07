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
package fr.avianey.mcts4j;

import java.util.LinkedList;
import java.util.Set;

/**
 * Abstract class implementing the basis of the
 * <a href="http://en.wikipedia.org/wiki/Monte-Carlo_tree_search">Monte Carlo Tree Search</a> algorithm :
 * <ol>
 * <li>selection</li>
 * <li>expansion</li>
 * <li>simulation</li>
 * <li>back propagation</li>
 * </ol>
 * The is a stateful implementation of the algorithm meaning that each of the four above steps modify
 * the state of the game and take care of restoring it before returning... To use this algorithm, just
 * implement all of the abstract methods of this class (and respect the contract of each one). Then,
 * get the best choice for the current player with {@link #getBestTransition()} and do it by calling
 * {@link #doTransition(Transition)}. {@link #undoTransition(Transition)} allow you to rollback a choice.
 * <br/>
 * The state SHOULD be stored in this class. {@link Node} SHOULD only be used to store necessary information
 * related to number of simulations and associated wins/loose ratio...
 *
 * @param <T> a {@link Transition} representing an atomic action that modifies the state
 *
 * @author antoine vianey
 */
// TODO keep track of number of nodes to evaluate memory footprint
public abstract class MonteCarloTreeSearch<T extends Transition> {

	/**
	 * This is where we are.
	 * Each {@link Node} keeps a reference to its parent {@link Node} and to each child {@link Node}.
	 */
    private Node<T> current;

    public MonteCarloTreeSearch() {
    	reset();
    }

    /**
     * Creates a new exploration tree.
     */
    public void reset() {
    	current = new Node<>(null, null, false);
    }

    /**
     * Get the best {@link Transition} for the current player.
     * Playing a {@link Transition} MUST be done by calling {@link #doTransition(Transition)}
     * unless next call to this method WILL rely on a wrong origin.
     * @return the best {@link Transition} for the current player or null if the current player has no possible move.
     */
    public T getBestTransition() {
        if (getPossibleTransitions().isEmpty()) {
            // no possible transition
            // isOver MUST be true.
            return null;
        }
        final int currentPlayer = getCurrentPlayer();
        Node<T> nodeToExpand;
        boolean stop = false;
        // TODO : do it in a interuptable Thread
        do {
	        nodeToExpand = selection();
	        if (nodeToExpand == null) {
	            break;
            }
            // the tree has not been fully explored yet
            Node<T> expandedNode = expansion(nodeToExpand);
            int winner = simulation();
            backPropagation(expandedNode, winner);
        } while (!stop);
        // state is restored
        assert currentPlayer == getCurrentPlayer();
        T best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        // all possible transitions have been set on root node
        // see expansion(N node)
        for (Node<T> child : current.getChilds()) {
            double value = child.ratio(currentPlayer);
            if (value > bestValue) {
                bestValue = value;
                best = child.getTransition();
                assert best != null;
            }
        }
        return best;
    }

    /**
     * Update the context and change the root of the tree to this context so that it reflects the
     * realization of the given {@link Transition}. This method is the same as {@link #makeTransition(Transition)}
     * but it also change the root of the tree to the {@link Node} reached by the given {@link Transition}.
     * MUST only be called with a {@link Transition} returned by {@link #getBestTransition()}.
     * @param transition The non null {@link Transition} to play
     * @see #makeTransition(Transition)
     */
    @SuppressWarnings("unchecked")
	public final void doTransition(T transition) {
    	makeTransition(transition);
    	current = current.getChild(transition);
    	current.makeRoot();
    }

    /**
     * Update the context and change the root of the tree to this context so that it reflects the <b>rollback</b> of the
     * realization of the given {@link Transition}. This method is the same as {@link #unmakeTransition(Transition)}
     * but it also change the root of the tree to the origin {@link Node} of the given {@link Transition} in the tree.
     * @see #unmakeTransition(Transition)
     */
    public final void undoTransition(T transition) {
        unmakeTransition(transition);
        current = new Node<>(current);
    }

    // region MCTS

    /**
     * Select a leaf {@link Node} to expand. The selection is done by calling {@link #selectTransition(Node, int)}
     * from child to child until we reach a leaf {@link Node}. The returned {@link Node} MIGHT be terminal (meaning
     * it was an unexplored child of a leaf {@link Node}).
     * @return The {@link Node} to expand or null if there's nothing else to expand...
     */
    @SuppressWarnings("unchecked")
    private Node<T> selection() {
        Node<T> n = current;
        Node<T> next;
        final int player = getCurrentPlayer();
        do {
            T transition = selectTransition(n, player);
            if (transition == null) {
                n.setTerminal(true);
                if (n == current) {
                    return null;
                } else {
                    // node has parent, rewind
                    unmakeTransition(n.getTransition());
                    next = n.getParent();
                }
            } else {
                next = n.getChild(transition);
                makeTransition(transition);
                if (next == null) {
                    // this transition has never been explored
                    // create child node and expand it
                    next = new Node<>(n, transition, isOver());
                }
            }
            n = next;
        } while (!n.isLeaf());
        return n;
    }

    /**
     * Expand the leaf {@link Node} by creating <strong>every</strong> child {@link Node}.<br/>
     * The leaf {@link Node} to expand MIGHT be a terminal {@link Node}, as {{@link #selection()}} MIGHT return a
     * {@link Node} that was just created...
     * After expansion, the leaf {@link Node} has all of its children created.<br/>
     * @param leaf The leaf {@link Node} to expand.
     * @return
     *      The expanded {@link Node} to run the random simulation from.
     *      The expanded {@link Node} MIGHT be a terminal {@link Node}.
     */
    private Node<T> expansion(final Node<T> leaf) {
        if (leaf.isTerminal()) {
            return leaf;
        }
        T transition = expansionTransition();
        if (transition != null) {
        	// expand the path with the chosen transition
            makeTransition(transition);
            return new Node<>(leaf, transition, isOver());
        } else {
            return leaf;
        }
    }

    /**
     * Run a random simulation from the expanded position to get a winner.
     * @return The winner designated by the random simulation.
     */
    private int simulation() {
        LinkedList<T> transitions = new LinkedList<>();
        // do
        while (!isOver()) {
            T transition = simulationTransition();
            assert transition != null;
            makeTransition(transition);
            transitions.add(transition);
        }
        int winner = getWinner();
        // undo
        while (!transitions.isEmpty()) {
            unmakeTransition(transitions.pollLast());
        }
        return winner;
    }

    /**
     * Propagate the winner from the expanded {@link Node} up to the current root {@link Node}
     * @param expandedNode The {@link Node} that was expanded.
     * @param winner The winner of the simulation.
     */
    private void backPropagation(Node<T> expandedNode, final int winner) {
        Node<T> n = expandedNode;
        while (n != null) {
            n.result(winner);
            Node<T> parent = n.getParent();
            if (parent == null) {
                // root reached
                break;
            }
            unmakeTransition(n.getTransition());
            n = parent;
        }
    }

    // endregion

    // region API

    /**
     * Method used to select a {@link Transition} to follow and reach a leaf {@link Node} to expand :
     * <ul>
     * <li>UCT : upper confident bound applied to trees</li>
     * </ul>
     * This method MUST NOT return a terminal {@link Node}.
     * @param node a {@link Node} that has already been visited
     * @param player the player for which we are seeking a promising child {@link Node}
     * @return the next {@link Transition} to a non terminal {@link Node} in the selection step
     * 		or null if there's no child to explore
     * 		or null if there's only terminal child nodes
     * @see Node#isTerminal()
     * @see UCT
     */
    public abstract T selectTransition(Node<T> node, int player);

    /**
     * Select the next {@link Transition} during the simulation step
     * @return
     * 		The next {@link Transition} in the simulation
     * 		or null if {{@link #getPossibleTransitions()} is null or empty
     */
    public abstract T simulationTransition();

    /**
     * Choose the {@link Transition} to follow and expanded and the simulation from.
     * @return
     *      The desired {@link Transition} to get the expanded {@link Node} from
     * 		or null if {{@link #getPossibleTransitions()} is null or empty
     */
    public abstract T expansionTransition();

    /**
     * Update the context so it takes into account the realization of the given {@link Transition}.
     * MUST only be called with a {@link Transition} returned by {@link #getBestTransition()}.
     * @param transition
     * 		A {@link Transition} returned by {@link #getBestTransition()} or null
     */
    protected abstract void makeTransition(T transition);

    /**
     * Update the context so it takes into account the rollback of the given {@link Transition}.
     * MUST only be called with the last {@link Transition} passed to {@link #makeTransition(Transition)}.
     * @param transition
     * 		A {@link Transition} returned by {@link #getBestTransition()} or null
     */
    protected abstract void unmakeTransition(T transition);

    /**
     * Get possible transitions from the current position. Returned transitions MIGHT involves
     * any of the players, taking into account actions such as pass, skip, fold, etc...
     * @return
     *      {@link Set} of possible transitions for the current position (including 'pass' or 'skip').
     *      {@link Set} could be empty, in that case {{@link #isOver()} MUST return true.
     */
    public abstract Set<T> getPossibleTransitions();

    /**
     * MUST return true if there's no possible {@link Transition} from the current position
     * @return true if {@link #getPossibleTransitions()} returns an empty {@link Set}
     */
    public abstract boolean isOver();

    /**
     * Return the index of the winner when {@link #isOver()} returns true.
     * @return the index of the winner
     */
    // TODO : handle draw (also in node and backpropagation)
    public abstract int getWinner();

    /**
     * Returns the index of the player for the current state.
     * @return
     */
    // TODO manage players internally
    public abstract int getCurrentPlayer();

    // endregion

}
