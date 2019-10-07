/*
 * This file is part of mcts4j.
 * <https://github.com/avianey/mcts4j>
 *
 * Copyright (C) 2019 Antoine Vianey
 *
 * mcts4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mcts4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */
package fr.avianey.mcts4j;

import java.io.Serializable;

/**
 * Represents an segment between a {@link Node} of depth n and a child {@link Node} of depth n+1.
 * Transitions MUST override {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * {@link Transition} MIGHT represent a pass action. Implementations SHOULD take care of reducing
 * memory footprint of this class (by keeping it stateless, using enums or whatever)
 *
 * @author antoine vianey
 */
public interface Transition extends Serializable {

}
