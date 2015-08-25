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
package net.automatalib.brics;

import java.util.Collection;
import java.util.Map;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * DOT rendering helper for Brics automaton adapters.
 * 
 * @author Malte Isberner 
 *
 */
final class BricsDOTHelper extends DefaultDOTHelper<State, Transition> {

	private final AbstractBricsAutomaton automaton;
	
	/**
	 * Constructor.
	 * @param automaton the automaton to render
	 */
	public BricsDOTHelper(AbstractBricsAutomaton automaton) {
		this.automaton = automaton;
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#initialNodes()
	 */
	@Override
	protected Collection<? extends State> initialNodes() {
		return automaton.getInitialStates();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getNodeProperties(State node, Map<String, String> properties) {
		if(!super.getNodeProperties(node, properties)) {
			return false;
		}
		
		String str = node.toString();
		int wsIdx1 = str.indexOf(' ');
		int wsIdx2 = str.indexOf(' ', wsIdx1 + 1);
		properties.put(NodeAttrs.LABEL, "s" + str.substring(wsIdx1 + 1, wsIdx2));
		if(node.isAccept()) {
			properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getEdgeProperties(java.lang.Object, java.util.Map)
	 */
	@Override
	public boolean getEdgeProperties(State src, Transition edge, State tgt,
			Map<String, String> properties) {
		if(!super.getEdgeProperties(src, edge, tgt, properties)) {
			return false;
		}
		
		String label = BricsTransitionProperty.toString(edge.getMin(), edge.getMax());
		properties.put(NodeAttrs.LABEL, label);
		return true;
	}

}
