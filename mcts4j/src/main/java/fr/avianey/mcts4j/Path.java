package fr.avianey.mcts4j;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

public class Path<T extends Transition, N extends Node<T>> {
    
    private final N root;
    private final LinkedList<Map.Entry<T, N>> nodes;

    public Path(N root) {
    	if (root == null) {
    		throw new IllegalArgumentException(
    				"Root " + Node.class.getName() + " of a " + Path.class.getName() + " MUST NOT be null");
    	}
        this.root = root;
        this.nodes = new LinkedList<Map.Entry<T, N>>();
    }
    
    /**
     * Expand the {@link Path} with the given {@link Transition} and {@link Node}.
     * This method does not add the given {@link Node} as child of the {@link Path#endNode()}
     * @param transition
     * @param node
     */
    public void expand(T transition, N node) {
        nodes.addLast(new AbstractMap.SimpleEntry<T, N>(transition, node));
    }

    public LinkedList<Map.Entry<T, N>> getNodes() {
        return nodes;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public N endNode() {
    	if (nodes.isEmpty()) {
    		// TODO : or null
    		return root;
    	} else {
    		return nodes.getLast().getValue();
    	}
    }

    public N rootNode() {
    	return root;
    }

    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for (Map.Entry<T, N> e : getNodes()) {
    		sb.append(" -> ");
    		sb.append(e.getKey());
    	}
    	return sb.toString();
    }
}
