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
package fr.avianey.mcts4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Node<T extends Transition> {

    private final Map<T, Node<T>> childs;
    private final Map<Integer, Integer> wins; // TODO Arraylist
    private long simulations = 0;
    private boolean terminal;
    private final T transition;
    private Node<T> parent;

    /**
     * Create a child {@link Node}.
     * @param parent The parent {@link Node} of the created node.
     * @param transition The transition from the parent {@link Node} to this node.
     * @param terminal Whether or not this {@link Node} is a terminal {@link Node}.
     */
    Node(Node<T> parent, T transition, boolean terminal) {
        this.terminal = terminal;
        this.parent = parent;
        this.transition = parent == null ? null : transition;
        this.childs = new HashMap<>();
        this.wins = new HashMap<>();
        if (parent != null) {
            parent.childs.put(transition, this);
        }
    }

    /**
     * Create a parent {@link Node}.
     * @param child
     */
    Node(Node<T> child) {
        this.terminal = false;
        this.parent = null;
        this.transition = null;
        this.childs = new HashMap<>();
        this.wins = new HashMap<>();
        this.simulations = child.simulations();
        // copy stats
        childs.put(child.getTransition(), child);
        for (Map.Entry<Integer, Integer> e : child.wins.entrySet()) {
            wins.put(e.getKey(), e.getValue());
        }
    }

    /**
     * A {@link Node} is terminal when there is no child to explore.
     * The sub-Tree of this {@link Node} has been fully explored or the {@link Node} correspond
     * to a configuration where {@link MonteCarloTreeSearch#isOver()} return true.
     * @return true If the {@link Node} is a terminal {@link Node}
     */
    // TODO propagate terminal information from node to node
    public boolean isTerminal() {
        return this.terminal;
    }

    public void setTerminal(boolean terminal) {
    	this.terminal = terminal;
    }

    /**
     * Get the value of the {@link Node} for the given player.
     * The {@link Node} with the greater value will be picked
     * as the best choice for this player.
     * @param player
     * @return
     */
    public double value(int player) {
        return wins(player);
    }

    /**
     * Get the cild {@link Node} reach by the given {@link Transition}
     * @param transition The {@link Transition}
     * @return The child {@link Node} or null if there's no child known for the given {@link Transition}
     */
    public Node<T> getChild(T transition) {
        return childs.get(transition);
    }

    /**
     * Return the parent {@link Node} of this {@link Node} and the {@link Transition} that lead to this {@link Node}
     * @return
     */
    public Node<T> getParent() {
        return parent;
    }

    /**
     * Return the {@link Transition} that lead to this {@link Node}
     * @return
     */
    public T getTransition() {
        return transition;
    }

    /**
     * Make this {@link Node} a root {@link Node} by removing the reference to its parent
     */
    public void makeRoot() {
        this.parent = null;
    }

    /**
     * A leaf {@link Node} is a node with no child.
     * There's two case where a {@link Node} can be leaf :
     * <ol>
     * <li>The {@link Node} is a terminal {@link Node}</li>
     * <li>The {@link Node} has never been expanded (has no child)</li>
     * </ol>
     * @return
     * 		true if the {@link Node} is a leaf {@link Node}
     */
    public boolean isLeaf() {
        return childs.isEmpty();
    }

    /**
     * Number of simulations back-propagated to this {@link Node}
     * @return
     */
    public long simulations() {
        return simulations;
    }

    /**
     * Number of simulation back-propagated to this {@link Node} where the given player has won
     * @param player
     * @return
     */
    public double ratio(int player) {
        Integer w = wins.get(player);
        if (w == null) {
            return 0;
        } else {
            return ((double) w) / simulations;
        }
    }

    public long wins(int player) {
        Integer w = wins.get(player);
        if (w == null) {
            return 0;
        } else {
            return w;
        }
    }

    /**
     * Propagate the result of a simulation to this {@link Node}.
     * After a call to this method, {@link #simulations()} is incremented as well as
     * {@link #wins(int)} for the given winner.
     * @param winner The winner of the back-propagated simulation
     */
    public void result(int winner) {
        simulations++;
        Integer w = wins.get(winner);
        if (w == null) {
            wins.put(winner, 1);
        } else {
            wins.put(winner, w + 1);
        }
    }

    /**
     * Returns the {@link Collection} of all the child of this {@link Node}
     * @return
     * 		The return {@link Collection} MUST NOT be null
     * 		If the {@link Node} is the leaf {@link Node}, then an empty {@link Collection} is returned
     */
    public Collection<Node<T>> getChilds() {
        return childs.values();
    }

    /**
     * Get the child {@link Node} reach by the given {@link Transition}
     * @param transition The {@link Transition} to fetch the child {@link Node} from
     * @return
     */
    public Node<T> getNode(T transition) {
        return childs.get(transition);
    }

}
