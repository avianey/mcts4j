package fr.avianey.mcts4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNode<T extends Transition> extends Node<T> {

	private final Map<T, Node<T>> childs;
	private final Map<Integer, Integer> wins;
	private int simulations = 0;

	public AbstractNode(Node<T> parent, boolean terminal) {
		super(parent, terminal);
		childs = new HashMap<>();
		wins = new HashMap<>();
	}

	@Override
	public boolean isLeaf() {
		return childs.isEmpty();
	}

	@Override
	public long simulations() {
		return simulations;
	}

	@Override
	public long wins(int player) {
		Integer w = wins.get(player);
		if (w == null) {
			return 0;
		} else {
			return w;
		}
	}

	@Override
	public void result(int winner) {
		simulations++;
		Integer w = wins.get(winner);
		if (w == null) {
			wins.put(winner, 1);
		} else {
			wins.put(winner, w + 1);
		}
	}

	@Override
	public Map<T, Node<T>> getTransitionsAndNodes() {
		return childs;
	}

	@Override
	public Collection<Node<T>> getChilds() {
		return childs.values();
	}

	@Override
	public Node<T> getNode(T transition) {
		return childs.get(transition);
	}

}
