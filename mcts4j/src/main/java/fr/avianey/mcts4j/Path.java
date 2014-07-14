package fr.avianey.mcts4j;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

public class Path<T extends Transition, N extends Node<T>> {
    
    private final N root;
    private final LinkedList<Map.Entry<T, N>> nodes;

    public Path(N root) {
        this.root = root;
        this.nodes = new LinkedList<Map.Entry<T, N>>();
    }
    
    public void expand(T transition, N node) {
        nodes.add(new AbstractMap.SimpleEntry(transition, node));
    }

    public LinkedList<Map.Entry<T, N>> getNodes() {
        return nodes;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
}
