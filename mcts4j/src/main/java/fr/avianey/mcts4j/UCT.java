package fr.avianey.mcts4j;

import java.util.Map;

public abstract class UCT<T extends Transition, N extends Node<T>> extends MonteCarloTreeSearch<T, N> {
    
    private static final double C = Math.sqrt(2);

    @Override
    @SuppressWarnings("unchecked")
    public Map.Entry<T, N> selectNonTerminalChildOf(N node, int player) {
        double v = Double.NEGATIVE_INFINITY;
        Map.Entry<T, N> best = null;
        for (Map.Entry<T, ? extends Node<T>> e : node.getTransitionsAndNodes().entrySet()) {
            if (!e.getValue().isTerminal()) {
            	// w/n + C * Math.sqrt(ln(n(p)) / n)
            	// TODO : add a random hint to avoid ex-aequo
                double value = (e.getValue().simulations() == 0 ? 0 : (e.getValue().wins(player) / e.getValue().simulations())
                        + C * Math.sqrt(Math.log(node.simulations()) / e.getValue().simulations()));
                if (value > v) {
                    v = value;
                    best = (Map.Entry<T, N>) e;
                }
            }
        }
        return best;
    }
    
}
