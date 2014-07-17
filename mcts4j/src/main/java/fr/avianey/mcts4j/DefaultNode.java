package fr.avianey.mcts4j;

public class DefaultNode<T extends Transition> extends AbstractNode<T> {

	/**
	 * Always loosing {@link Node} get a value of 0.
	 * An unexplored {@link Node} is potentially a best choice than a {@link Node} with no win.
	 */
	public static final double UNEXPLORED_VALUE = 0.5;
	
	public DefaultNode(Node<T> parent, boolean terminal) {
		super(parent, terminal);
	}

	@Override
	public double value(int player) {
		return wins(player);
	}

}
