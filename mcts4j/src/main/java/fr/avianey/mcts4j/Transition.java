package fr.avianey.mcts4j;

/**
 * Represents an segment between a {@link Node} of depth n and a child {@link Node} of depth n+1.
 * Transitions MUST override {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * {@link Transition} MIGHT represent a pass action.
 * 
 * @author antoine vianey
 */
public interface Transition {

}
