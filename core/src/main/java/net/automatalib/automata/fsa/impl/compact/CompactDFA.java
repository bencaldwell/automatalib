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
package net.automatalib.automata.fsa.impl.compact;

import java.util.BitSet;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompactSimpleDet;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.words.Alphabet;

public class CompactDFA<I> extends AbstractCompactSimpleDet<I, Boolean> implements
		MutableDFA<Integer,I> {
	public static final class Creator<I> implements AutomatonCreator<CompactDFA<I>, I> {
		@Override
		public CompactDFA<I> createAutomaton(Alphabet<I> alphabet) {
			return new CompactDFA<>(alphabet);
		}
		@Override
		public CompactDFA<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
			return new CompactDFA<>(alphabet, numStates);
		}
	}
	
	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	private final BitSet acceptance;
	
	public CompactDFA(Alphabet<I> alphabet) {
		super(alphabet);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(CompactDFA<I> other) {
		this(other.getInputAlphabet(), other);
	}
	
	protected CompactDFA(Alphabet<I> alphabet, CompactDFA<?> other) {
		super(alphabet, other);
		this.acceptance = (BitSet)other.acceptance.clone();
	}
	
	public <I2> CompactDFA<I2> translate(Alphabet<I2> newAlphabet) {
		if (newAlphabet.size() != alphabetSize) {
			throw new IllegalArgumentException("Alphabet sizes must match, but they do not (old/new): " +
					alphabetSize + " vs. " + newAlphabet.size());
		}
		return new CompactDFA<I2>(newAlphabet, this);
	}
	
	@Override
	public void ensureCapacity(int oldCap, int newCap) {
		acceptance.set(newCap);
	}


	@Override
	public void flipAcceptance() {
		acceptance.flip(0, size());
	}

	

	@Override
	public void clear() {
		acceptance.clear();
		super.clear();
	}


	public void setAccepting(int state, boolean accepting) {
		acceptance.set(state, accepting);
	}

	@Override
	public void setAccepting(Integer state, boolean accepting) {
		setAccepting(state.intValue(), accepting);
	}

	
	@Override
	public Integer addState(boolean accepting) {
		return addState(Boolean.valueOf(accepting));
	}


	public boolean isAccepting(int stateId) {
		return acceptance.get(stateId);
	}
	
	@Override
	public boolean isAccepting(Integer state) {
		return isAccepting(state.intValue());
	}

	@Override
	public Boolean getStateProperty(int stateId) {
		return isAccepting(stateId);
	}

	@Override
	public void initState(int stateId, Boolean property) {
		boolean bval = (property == null) ? false : property.booleanValue();
		setAccepting(stateId, bval);
	}

	
	@Override
	public void setStateProperty(int stateId, Boolean property) {
		boolean bval = (property == null) ? false : property.booleanValue();
		setAccepting(stateId, bval);
	}

	@Override
	public Integer addInitialState(boolean accepting) {
		return super.addInitialState(Boolean.valueOf(accepting));
	}

	
}
