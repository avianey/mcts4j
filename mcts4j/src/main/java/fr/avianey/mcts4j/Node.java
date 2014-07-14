package fr.avianey.mcts4j;

import java.util.Map;
import java.util.Set;

public abstract class Node<T extends Transition> {
    
    private boolean terminal;
    private Node<T> parent;
    
    public Node(Node<T> parent, boolean terminal) {
        this.terminal = terminal;
        this.parent = parent;
    }
    
    abstract boolean isLeaf();
    
    /**
     * A {@link Node} is terminal when there is no child to explore.
     * The sub-Tree has been fully explored or the {@link Node} correspond
     * to a GAMEOVER configuration.
     * @return
     */
    public boolean isTerminal() {
        return this.terminal;
    }
    
    abstract void setTerminal(boolean terminal);
    
    abstract long simulations();
    
    abstract long wins(int player);
    
    abstract void result(int winner);
    
    abstract Map<T, Node<T>> getTransitionsAndNodes();
    
    abstract Set<Node<T>> getChilds();
    
    abstract Node<T> getNode(T transition);

    public Node<T> getParent() {
        return parent;
    }
    
}
