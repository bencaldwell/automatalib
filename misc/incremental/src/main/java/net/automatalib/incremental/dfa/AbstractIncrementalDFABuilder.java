/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.dfa;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;

import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Abstract base class for {@link IncrementalDFABuilder}s. This class takes care of
 * holding the input alphabet and its size.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol class
 */
public abstract class AbstractIncrementalDFABuilder<I> implements
		IncrementalDFABuilder<I> {
	
	
	protected static abstract class AbstractGraphView<I,N,E>
			implements GraphView<I, N, E> {
		@Override
		public GraphDOTHelper<N, E> getGraphDOTHelper() {
			return new DefaultDOTHelper<N,E>() {
				@Override
				public Collection<? extends N> initialNodes() {
					return Collections.singleton(getInitialNode());
				}
				@Override
				public boolean getNodeProperties(N node,
						Map<String, String> properties) {
					if(!super.getNodeProperties(node, properties)) {
						return false;
					}
					switch(getAcceptance(node)) {
					case TRUE:
						properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
						break;
					case DONT_KNOW:
						properties.put(NodeAttrs.STYLE, NodeStyles.DASHED);
						break;
					default: // case FALSE: default style
					}
					return true;
				}
				@Override
				public boolean getEdgeProperties(N src, E edge, N tgt,
						Map<String, String> properties) {
					if(!super.getEdgeProperties(src, edge, tgt, properties)) {
						return false;
					}
					I input = getInputSymbol(edge);
					properties.put(EdgeAttrs.LABEL, String.valueOf(input));
					return true;
				}
				
			};
		}
	}
	
	protected static abstract class AbstractTransitionSystemView<S,I,T>
		implements IncrementalDFABuilder.TransitionSystemView<S, I, T> {
		@Override
		public Acceptance getStateProperty(S state) {
			return getAcceptance(state);
		}
		@Override
		public Void getTransitionProperty(T transition) {
			return null;
		}
	}
	
	protected final Alphabet<I> inputAlphabet;
	protected final int alphabetSize;
	
	/**
	 * Constructor.
	 * @param inputAlphabet the input alphabet
	 */
	public AbstractIncrementalDFABuilder(@Nonnull Alphabet<I> inputAlphabet) {
		this.inputAlphabet = inputAlphabet;
		this.alphabetSize = inputAlphabet.size();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#getInputAlphabet()
	 */
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#hasDefinitiveInformation(net.automatalib.words.Word)
	 */
	@Override
	public boolean hasDefinitiveInformation(Word<? extends I> word) {
		return lookup(word) != Acceptance.DONT_KNOW;
	}
	
	@Override
	public void insert(Word<? extends I> word) throws ConflictException {
		insert(word, true);
	}

}
