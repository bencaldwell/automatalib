/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.ts.powerset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.ts.PowersetViewTS;
import net.automatalib.ts.TransitionSystem;


public class DirectPowersetDTS<S, I, T>
		implements PowersetViewTS<Set<? extends S>, I, Set<? extends T>, S, T> {
	
	private final TransitionSystem<S, I, T> ts;
	
	public DirectPowersetDTS(TransitionSystem<S,I,T> ts) {
		this.ts = ts;
	}

	@Override
	public Set<? extends S> getInitialState() {
		return ts.getInitialStates();
	}

	@Override
	public Set<? extends T> getTransition(Set<? extends S> state, I input) {
		Set<T> result = new HashSet<>();
		for(S s : state) {
			Collection<? extends T> transitions = ts.getTransitions(s, input);
			result.addAll(transitions);
		}
		return result;
	}

	@Override
	public Set<? extends S> getSuccessor(Set<? extends T> transition) {
		Set<S> result = new HashSet<S>();
		for(T trans : transition)
			result.add(ts.getSuccessor(trans));
		return result;
	}
	
	@Override
	public Set<? extends S> getSuccessor(Set<? extends S> state, I input) {
		Set<S> result = new HashSet<S>();
		for(S s : state) {
			Collection<? extends T> transitions = ts.getTransitions(s, input);
			for(T t : transitions) {
				result.add(ts.getSuccessor(t));
			}
		}
		
		return result;
	}

	@Override
	public Collection<? extends S> getOriginalStates(Set<? extends S> state) {
		return state;
	}

	@Override
	public Collection<? extends T> getOriginalTransitions(
			Set<? extends T> transition) {
		return transition;
	}
}
