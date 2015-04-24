/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.serialization.taf.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.strings.StringUtil;
import net.automatalib.words.Alphabet;


abstract class AbstractTAFBuilder<S,I,T,SP,TP,M extends MutableDeterministic<S, I, T, SP, TP>>
		implements TAFBuilder {
	
	private static final Pattern idPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
	
	private final InternalTAFParser parser;
	
	protected M automaton;
	private Alphabet<String> alphabet;
	private final Map<String,S> stateMap = new HashMap<>();
	private final Set<String> declaredStates = new HashSet<>();
	
	protected abstract M createAutomaton(Alphabet<String> stringAlphabet);
	protected abstract SP getStateProperty(Set<String> options);
	protected abstract I translateInput(String c);
	
	public AbstractTAFBuilder(InternalTAFParser parser) {
		this.parser = parser;
	}

	@Override
	public void init(Alphabet<String> alphabet) {
		if (automaton != null) {
			throw new IllegalStateException();
		}
		automaton = createAutomaton(alphabet);
		this.alphabet = alphabet;
	}
	
	@Override
	public M finish() {
		checkState();
		
		stateMap.clear();
		declaredStates.clear();
		M result = automaton;
		automaton = null;
		alphabet = null;
		return result;
	}

	@Override
	public void declareState(String identifier, Set<String> options) {
		if (!declaredStates.add(identifier)) {
			error("State {0} declared twice", identifier);
		}
		
		boolean init = options.remove("initial") | options.remove("init");
		if (init && automaton.getInitialState() != null) {
			error("Duplicate initial state {0}", identifier);
			init = false;
		}
		
		S state = stateMap.get(identifier);
		SP property = getStateProperty(options);
		if (state == null) {
			state = (init) ? automaton.addInitialState(property) : automaton.addState(property);
			stateMap.put(identifier, state);
		}
		else {
			automaton.setStateProperty(state, property);
			if (init) {
				automaton.setInitialState(state);
			}
		}
		
		if (!options.isEmpty()) {
			warning("Unrecognized options for state {0}: {1}", identifier, options);
		}
	}
	
	protected void doAddTransitions(String source, Collection<String> symbols, String target, TP transProperty) {
		S src = lookupState(source);
		S tgt = lookupState(target);
		List<String> invalidSymbols = new ArrayList<>();
		for (String s : symbols) {
			if (!alphabet.containsSymbol(s)) {
				invalidSymbols.add(StringUtil.enquoteIfNecessary(s, idPattern));
				continue;
			}
			I input = translateInput(s);
			T exTrans = automaton.getTransition(src, input);
			if (exTrans != null) {
				if (!Objects.equals(tgt, automaton.getSuccessor(exTrans))) {
					error("Duplicate transition from {0} on input {1} to differing target {2}"
							+ " would introduce non-determinism", source,
							StringUtil.enquoteIfNecessary(s, idPattern), tgt);
				}
				else if (!Objects.equals(transProperty, automaton.getTransitionProperty(exTrans))) {
					error("Duplicate transition from {0} on input {1} to {2} with "
							+ "differing property '{3}' would introduce non-determinism",
							source, StringUtil.enquoteIfNecessary(s, idPattern), tgt, transProperty);
				}
			}
			else {
				automaton.addTransition(src, input, tgt, transProperty);
			}
		}
		if (!invalidSymbols.isEmpty()) {
			error("Invalid symbols for transition from {0} to {1}: {2}", source, target, invalidSymbols);
		}
	}
	
	protected S lookupState(String identifier) {
		S state = stateMap.get(identifier);
		if (state == null) {
			state = automaton.addState();
			stateMap.put(identifier, state);
		}
		return state;
	}
	
	protected void error(String msgFmt, Object... args) {
		parser.error(msgFmt, args);
	}
	protected void warning(String msgFmt, Object... args) {
		parser.warning(msgFmt, args);
	}
	
	protected void checkState() {
		if (automaton == null) {
			throw new IllegalStateException();
		}
	}
}
