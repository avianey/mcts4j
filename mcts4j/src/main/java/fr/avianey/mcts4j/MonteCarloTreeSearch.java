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
 * the state of the game and take care of restoring it before returning... To use the algorithm, just
 * implement all of the abstract methods of this class (and respect the contract of each one). Then,
 * get the best choice for the current player with {@link #getBestTransition()} and do it by calling
 * {@link #doTransition(Transition)}. {@link #undoTransition(Transition)} allow you to rollback a choice.
 * 
 * @param <T> a {@link Transition} representing an atomic action that modifies the state 
 * @param <N> a {@link Node} that stores simulations and wins
 * 
 * @author antoine vianey
 */
// TODO describe pass, skip actions
public abstract class MonteCarloTreeSearch<T extends Transition, N extends Node<T>> {
    
	/**
	 * This is where we are.
	 * Each {@link Node} keeps a reference to its parent {@link Node} and to each child {@link Node}.
	 */
    private Path<T, N> pathToRoot;
    
    public MonteCarloTreeSearch() {
    	reset();
    }
    
    public void reset() {
//    	root = newNode(null, false);
    	pathToRoot = new Path<T, N>(newNode(null, false));
    }
    
    /**
     * Get the best {@link Transition} for the current player.
     * Playing a {@link Transition} MUST be done by calling {@link #makeTransitionAndChangeRoot(Transition)}
     * unless next call to this method WILL rely on a wrong origin.
     * @return the best {@link Transition} for the current player or
     *      null if the current player has no possible move.
     */
    public T getBestTransition() {
        if (getPossibleTransitions().isEmpty()) {
            return null;
        }
        final int currentPlayer = getCurrentPlayer();
        Path<T, N> path;
        boolean stop = false;
        // TODO : do it in a interuptable Thread
        do {
	        path = selection(pathToRoot, currentPlayer);
	        if (path != null) {
	            // the tree has not been fully explored yet
	            path = expansion(path);
	            int winner = simulation();
	            backPropagation(path, winner);
	        }
        } while (path != null && !stop);
        // state is restored
        assert currentPlayer == getCurrentPlayer();
        T best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        // all possible transitions are set on root node
        // see expansion(Path<T, N> path)
        for (Map.Entry<T, ? extends Node<T>> e : pathToRoot.endNode().getTransitionsAndNodes().entrySet()) {
            double value = e.getValue().value(currentPlayer);
            if (value > bestValue) {
                bestValue = value;
                best = e.getKey();
            }
        }
        return best;
    }
    
    /**
     * Truncate the tree, keeping only the current root {@link Node} and its sub-tree.
     * Sub-trees that does not contains the current root {@link Node} are removes as well.
     */
    public void simplifyTree() {
    	this.pathToRoot = new Path<T, N>(pathToRoot.endNode());
    	this.pathToRoot.endNode().makeRoot();
    }
    
    /**
     * Update the context and change the root of the tree to this context so that it reflects the
     * realization of the given {@link Transition}. This method is the same as {@link #makeTransition(Transition)}
     * but it also change the root of the tree to the {@link Node} reached by the given {@link Transition}.
     * MUST only be called with a {@link Transition} returned by {@link #getBestTransition()}.
     * @param transition
     * @see #makeTransition(Transition)
     */
    @SuppressWarnings("unchecked")
	public final void doTransition(T transition) {
    	if (transition != null) {
    		makeTransition(transition);
    		pathToRoot.expand(transition, (N) pathToRoot.endNode().getNode(transition));
    	}
    }
    
    /**
     * Update the context and change the root of the tree to this context so that it reflects the rollback of the
     * realization of the given {@link Transition}. This method is the same as {@link #unmakeTransition(Transition)}
     * but it also change the root of the tree to the origin {@link Node} of the given {@link Transition} in the tree.
     * MUST only be called with the last {@link Transition} passed to {@link #makeTransition(Transition)}.
     * @param transition
     * @see #unmakeTransition(Transition)
     */
    // TODO : handle errors when undoing transition after simplifyTree()
    public final void undoTransition(T transition) {
    	if (transition != null) {
    		unmakeTransition(transition);
    		pathToRoot.getNodes().pollLast();
    	}
    }
    
    /*======*
     * MCTS *
     *======*/

    /**
     * Select a non terminal leaf {@link Node} to expand expressed by a {@link Path} from
     * the current root {@link Node} to the leaf {@link Node}. The selection is done by calling
     * {@link #selectNonTerminalChildOf(Node)} from child to child until we reach a leaf {@link Node}.
     * @param root The {@link Path} to the current root {@link Node}
     * @param player The player for which we are seeking an optimal {@link Transition}
     * @return the {@link Path} to the leaf {@link Node} to expand or null if
     *      there's nothing else to expand...
     */
    // TODO : we can start from origin each time with no root param (use pathToRoot instead)
    @SuppressWarnings("unchecked")
    private Path<T, N> selection(final Path<T, N> root, final int player) {
        N current = root.endNode();
        // TODO : initialize with root path instead ???
        Path<T, N> path = new Path<T, N>(current);
        Map.Entry<T, N> next;
        while (!current.isLeaf()) {
            next = selectNonTerminalChildOf(current, player);
            if (next == null) {
                // nothing to explore
                current.setTerminal(true);
                if (current == root.endNode()) {
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
     * Expand the leaf {@link Node} by creating <strong>every</strong> child {@link Node}.<br/>
     * The leaf {@link Node} to expand MUST NOT be a terminal {@link Node}.<br/>
     * After expansion, the leaf {@link Node} has all of its children created.<br/>
     * @param path a {@link Path} to a non terminal leaf {@link Node}.
     * @return the {@link Path} to run the random simulation from.
     *      The expanded {@link Node} MIGHT be a terminal {@link Node}
     */
    private Path<T, N> expansion(final Path<T, N> path) {
        if (path == null) {
            throw new IllegalArgumentException("The " + Node.class.getSimpleName() + " to expand MUST NOT be null.");
        }
        Set<T> possibleTransitions = getPossibleTransitions();
        if (!possibleTransitions.isEmpty()) {
            // choose the child to expand
            T transition = expansionTransition(possibleTransitions);
        	// add every child node to the tree
        	for (T possibleTransition : possibleTransitions) {
                if (possibleTransition.equals(transition)) {
                	continue;
                }
                makeTransition(possibleTransition);
                N node = newNode(path.endNode(), isOver());
                path.endNode().addChildNode(possibleTransition, node);
                unmakeTransition(possibleTransition);
        	}
        	// expand the path with the chosen transition
            makeTransition(transition);
            N node = newNode(path.endNode(), isOver());
            path.endNode().addChildNode(transition, node);
            path.expand(transition, node);
        } else {
            throw new IllegalStateException("Trying to expand a " + Node.class.getName() + " with no possible transitions. "
                    + "Only non terminal " + Node.class.getName() + " could be expanded... "
                    + "Check the contract for " + MonteCarloTreeSearch.class.getName() + ".selectNonTerminalChildOf()");
        }
        return path;
    }
    
    /**
     * Run a random simulation from the expanded position to get a winner.
     * @return
     * 		The winner of the random simulation
     */
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
    
    /**
     * Propagate the winner from the expanded {@link Node} up to the current root {@link Node}
     * @param path The {@link Path} starting at the current root {@link Node} and leading to the expanded {@link Node}
     * @param winner The winner of the simulation
     */
    private void backPropagation(final Path<T, N> path, final int winner) {
        Map.Entry<T, N> e;
        while((e = path.getNodes().pollLast()) != null) {
        	// every Node of the Path except the root
            unmakeTransition(e.getKey());
            e.getValue().result(winner);
        }
        // the root node
        path.rootNode().result(winner);
    }

    /*==================*
     * ABSTRACT METHODS *
     *==================*/
    
    /**
     * Method used to select a child of a {@link Node} and reach a leaf {@link Node} to expand :
     * <ul>
     * <li>UCT : upper confident bound applied to trees</li>
     * </ul>
     * This method MUST NOT return a terminal {@link Node}.
     * @param node a {@link Node} that has already been visited
     * @param player the player for which we are seeking a promising child {@link Node}
     * @return the next (non terminal) {@link Node} in the selection step 
     * 		or null if there's no more child node to explore
     * 		or null if there's only terminal child nodes
     * @see Node#isTerminal()
     */
    public abstract Map.Entry<T, N> selectNonTerminalChildOf(N node, int player);
    
    /**
     * Select the next {@link Transition} during the simulation step
     * @param possibleTransitions
     * 		The possible transitions from the current root {@link Node}
     * @return
     * 		The desired {@link Transition}
     * 		or null if possibleTransitions is null or empty
     */
    public abstract T simulationTransition(Set<T> possibleTransitions);
    
    /**
     * Choose the {@link Node} to be expanded and to run the simulation from
     * @param possibleTransitions
     * 		Possible {@link Transition} of the selected {@link Node}
     * 		At least one possible {@link Transition}
     * @return
     * 		The desired {@link Transition} to get the expanded {@link Node} from
     */
    public abstract T expansionTransition(Set<T> possibleTransitions);
    
    /**
     * Update the context so it takes into account the realization of the given {@link Transition}.
     * MUST only be called with a {@link Transition} returned by {@link #getBestTransition()}.
     * @param transition
     * 		A {@link Transition} returned by {@link #getBestTransition()} or null
     * @see #doTransition(Transition)
     */
    protected abstract void makeTransition(T transition);

    /**
     * Update the context so it takes into account the rollback of the given {@link Transition}.
     * MUST only be called with the last {@link Transition} passed to {@link #makeTransition(Transition)}.
     * @param transition
     * 		A {@link Transition} returned by {@link #getBestTransition()} or null
     * @see #undoTransition(Transition)
     */
    // TODO : handle errors when undoing transition after simplifyTree()
    protected abstract void unmakeTransition(T transition);
    
    /**
     * Get possible transitions from the current position. Returned transitions MIGHT involves
     * any of the players, taking into account actions such as pass, skip, fold, etc...
     * @return possible transitions for the current position or an empty {@link Set}
     *      if there's no possible transition.
     */
    // TODO : return no transition <==> isOver() transitions when pass
    public abstract Set<T> getPossibleTransitions();
    
    /**
     * Create a new {@link Node}
     * @param parent The parent of the created {@link Node}
     * @param terminal True if the {@link Node} represents a configuration where {@link #isOver()} return true
     * @return
     * 		The new {@link Node}
     * 		MUST be not null.
     */
    public abstract N newNode(Node<T> parent, boolean terminal);
    
    /**
     * MUST return true if there's no possible {@link Transition} from the current position
     * @return true if {@link #getPossibleTransitions()} returns an empty {@link Set}
     */
    public abstract boolean isOver();
    
    /**
     * Return the index of the winner when {@link #isOver()} returns true
     * @return
     * 		the index of the winner
     */
    // TODO : handle draw (also in node and backpropagation)
    public abstract int getWinner();
    
    /**
     * Returns the index of the player for the current root {@link Node}
     * @return
     */
    public abstract int getCurrentPlayer();
    
    /**
     * Change current turn to the next player.
     * This method must not be used in conjunction with the makeMove() method.
     * Use it to implement a <strong>pass</strong> functionality.
     * @see #makeTransition(Transition)
     */
    public abstract void next();
    
    /**
     * Change current turn to the previous player.
     * This method must not be used in conjunction with the unmakeMove() method.
     * Use it to implement an <strong>undo</strong> functionality.
     * @see #unmakeTransition(Transition)
     */
    public abstract void previous();

}
