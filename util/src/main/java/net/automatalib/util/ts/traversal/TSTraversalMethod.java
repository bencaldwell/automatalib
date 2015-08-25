/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.ts.traversal;

import java.util.Collection;

import net.automatalib.ts.TransitionSystem;

public interface TSTraversalMethod {
	
	public static TSTraversalMethod BREADTH_FIRST = new TSTraversalMethod() {
		@Override
		public <S, I, T, D> void traverse(TransitionSystem<S, ? super I, T> ts,
				int limit, Collection<? extends I> inputs,
				TSTraversalVisitor<S, I, T, D> visitor) {
			TSTraversal.breadthFirst(ts, limit, inputs, visitor);
		}
	};
	
	public static TSTraversalMethod DEPTH_FIRST = new TSTraversalMethod() {
		@Override
		public <S, I, T, D> void traverse(TransitionSystem<S, ? super I, T> ts,
				int limit, Collection<? extends I> inputs,
				TSTraversalVisitor<S, I, T, D> visitor) {
			TSTraversal.depthFirst(ts, limit, inputs, visitor);
		}
	};
	
	public <S,I,T,D>
	void traverse(TransitionSystem<S,? super I,T> ts, int limit, Collection<? extends I> inputs, TSTraversalVisitor<S, I, T, D> visitor);

}
