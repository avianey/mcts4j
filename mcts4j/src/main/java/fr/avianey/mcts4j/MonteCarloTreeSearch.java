package fr.avianey.mcts4j;

import java.util.LinkedList;
import java.util.Map;
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
 * the state of the game and take care of restoring it before returning...
 * 
 * @param <T> a {@link Transition} representing an atomic action that modifies the state 
 * @param <N> a {@link Node} that stores simulations and wins
 * 
 * @author Tonio
 */
public abstract class MonteCarloTreeSearch<T extends Transition, N extends Node<T>> {
    
    N root;
    
    /**
     * Select a non terminal leaf {@link Node} to expand expressed by a {@link Path} from
     * the root {@link Node} to the leaf {@link Node}. The selection is done by calling
     * {@link #selectNonTerminalChildOf(Node)} from child to child until we reach a leaf {@link Node}.
     * @return the {@link Path} to the leaf {@link Node} to expand or null if
     *      there's nothing else to expand...
     */
    @SuppressWarnings("unchecked")
    private Path<T, N> selection(int player) {
        N current = root;
        Path<T, N> path = new Path<T, N>(current);
        Map.Entry<T, N> next;
        while (!current.isLeaf()) {
            next = selectNonTerminalChildOf(current, player);
            if (next == null) {
                // nothing to explore
                current.setTerminal(true);
                if (current == root) {
                    // stuck at root node
                    return null;
                } else {
                    // get back to the parent
                    current = (N) current.getParent();
                    unmakeTransition(path.getNodes().pollLast().getKey());
                }
            } else {
                current = next.getValue();
                path.expand(next.getKey(), current);
                makeTransition(next.getKey());
            }
        }
        return path;
    }

    /**
     * Method used to select a the child of a {@link Node} and reach a leaf {@link Node} to expand.
     * This method MUST NOT return a terminal {@link Node}.
     * @param node a {@link Node} that has already been visited
     * @param player the player for which we are seeking a promising child {@link Node}
     * @return the next (non terminal) {@link Node} in the selection or null if there's no more child node to explore
     * @see Node#isTerminal()
     */
    public abstract Map.Entry<T, N> selectNonTerminalChildOf(N node, int player);
    
    /**
     * selectionTransition(empty list) == null
     * selectionTransition(null) == null
     * @param possibleTransitions
     * @return
     */
    public abstract T simulationTransition(Set<T> possibleTransitions);
    public abstract T expansionTransition(Set<T> possibleTransitions);
    /**
     * maketransition(null) == next()
     * @param transition
     */
    public abstract void makeTransition(T transition);
    /**
     * unmaketransition(null) == previous()
     * @param transition
     */
    public abstract void unmakeTransition(T transition);
    
    /**
     * Get possible transitions from the current position. Returned transitions MIGHT involves
     * any of the players, taking into account actions such as pass, skip, fold, ...
     * @return possible transitions for the current position.
     */
    public abstract Set<T> getPossibleTransitions();
    
    public abstract N newNode(boolean terminal);
    
    /**
     * MUST return true if there's no possible {@link Transition} from the current position
     * @return true if {@link #getPossibleTransitions()} returns an empty {@link Set}
     */
    // TODO : Iterator
    public abstract boolean isOver();
    
    public abstract int getWinner();
    
    /**
     * Change current turn to the next player.
     * This method must not be used in conjunction with the makeMove() method.
     * Use it to implement a <strong>pass</strong> functionality.
     * @see #makeMove(Move)
     */
    public abstract void next();
    
    /**
     * Change current turn to the previous player.
     * This method must not be used in conjunction with the unmakeMove() method.
     * Use it to implement an <strong>undo</strong> functionality.
     * @see #unmakeMove(Move)
     */
    public abstract void previous();
    
    /**
     * Expand the leaf {@link Node} by creating a new child {@link Node} added to the tree.
     * The leaf {@link Node} to expand MUST NOT be a terminal {@link Node}
     * @param path a {@link Path} to a non terminal leaf {@link Node}.
     * @return the {@link Path} to run the random simulation from.
     *      The expanded {@link Node} CAN be a terminal {@link Node}
     */
    private Path<T, N> expansion(final Path<T, N> path) {
        Set<T> possibleTransitions = getPossibleTransitions();
        if (!possibleTransitions.isEmpty()) {
            // choose a child to expand
            T transition = expansionTransition(possibleTransitions);
            makeTransition(transition);
            // create the node and expand the path
            // set node to terminal if the game is over
            // TODO : abstract class with single constructor
            N node = newNode(isOver());
            path.expand(transition, node);
        } else {
            throw new IllegalStateException("Trying to expand a " + Node.class.getName() + " with no possible transitions. "
                    + "Only non terminal " + Node.class.getName() + " could be expanded... "
                    + "Check the contract for " + MonteCarloTreeSearch.class.getName() + ".selectNonTerminalChildOf() ");
        }
        return path;
    }
    
    
    // TODO what if more than 2 players
    private int simulation() {
        LinkedList<T> transitions = new LinkedList<T>();
        while (!isOver()) {
            Set<T> possibleTransitions = getPossibleTransitions();
            T transition = simulationTransition(possibleTransitions);
            makeTransition(transition);
            transitions.add(transition);
        }
        int winner = getWinner();
        // undo moves
        while(!transitions.isEmpty()) {
            unmakeTransition(transitions.pollLast());
        }
        // game over
        return winner;
    }
    
    
    
    private void backPropagation(Path<T, N> path, int winner) {
        Map.Entry<T, N> e;
        while((e = path.getNodes().pollLast()) != null) {
            unmakeTransition(e.getKey());
            e.getValue().result(winner);
        }
    }
    

}
