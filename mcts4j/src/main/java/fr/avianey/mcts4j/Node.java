package fr.avianey.mcts4j;

import java.util.Collection;
import java.util.Map;

public abstract class Node<T extends Transition> {
    
    private boolean terminal;
    private Node<T> parent;
    
    public Node(Node<T> parent, boolean terminal) {
        this.terminal = terminal;
        this.parent = parent;
    }
    
    /**
     * A leaf {@link Node} is a node with no child.
     * There's two case where a {@link Node} can be leaf :
     * <ol>
     * <li>The {@link Node} is a terminal {@link Node}</li>
     * <li>The {@link Node} has never been expanded</li>
     * </ol> 
     * In both case {@link #getTransitionsAndNodes()} returns an empty {@link Map}
     * @return
     * 		true if the {@link Node} is a leaf {@link Node}
     */
    public abstract boolean isLeaf();
    
    /**
     * A {@link Node} is terminal when there is no child to explore.
     * The sub-Tree of this {@link Node} has been fully explored or the {@link Node} correspond
     * to a configuration where {@link MonteCarloTreeSearch#isOver()} return true.
     * @return
     */
    public boolean isTerminal() {
        return this.terminal;
    }
    
    public void setTerminal(boolean terminal) {
    	this.terminal = terminal;
    }
    
    /**
     * Add a child to the {@link Node} and associate it with the given {@link Transition}
     * @param transition The transition leading to the child
     * @param child The child {@link Node}
     */
    public void addChildNode(T transition, Node<T> child) {
    	getTransitionsAndNodes().put(transition, child);
    }
    
    /**
     * Number of simulations back-propagated to this {@link Node}
     * @return
     */
    public abstract long simulations();
    
    /**
     * Number of simulation back-propagated to this {@link Node} where the given player has won
     * @param player 
     * @return
     */
    public abstract long wins(int player);
    
    /**
     * Propagate the result of a simulation to this {@link Node}.
     * After a call to this method, {@link #simulations()} is incremented as well as
     * {@link #wins(int)} for the given winner.
     * @param winner The winner of the back-propagated simulation
     */
    public abstract void result(int winner);
    
    /**
     * Get the value of the {@link Node} for the given player.
     * The {@link Node} with the greater value will be picked
     * as the best choice for this player.
     * @param player
     * @return
     */
    public abstract double value(int player);
    
    /**
     * Get the {@link Map} of all the {@link Transition} and child of this {@link Node}
     * @return
     * 		The return {@link Map} MUST NOT be null
     * 		If the {@link Node} is the leaf {@link Node}, then an empty {@link Map} is returned
     */
    public abstract Map<T, Node<T>> getTransitionsAndNodes();
    
    /**
     * Returns the {@link Collection} of all the child of this {@link Node}
     * @return
     * 		The return {@link Collection} MUST NOT be null
     * 		If the {@link Node} is the leaf {@link Node}, then an empty {@link Collection} is returned
     */
    public abstract Collection<Node<T>> getChilds();
    
    /**
     * Get the child {@link Node} reach by the given {@link Transition}
     * @param transition The {@link Transition} to fetch the child {@link Node} from
     * @return
     */
    public abstract Node<T> getNode(T transition);

    /**
     * Return the parent {@link Node} of this {@link Node}
     * @return
     */
    public Node<T> getParent() {
        return parent;
    }
    
    /**
     * Make this {@link Node} a root {@link Node} by removing the reference to its parent
     */
    public void makeRoot() {
    	this.parent = null;
    }
    
}
