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
package net.automatalib.serialization.taf.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.strings.StringUtil;

import com.google.common.base.Function;

/**
 * This class provides methods to write automata in the TAF format.
 * 
 * @author Malte Isberner
 */
public class TAFWriter {
	
	private static final Pattern idPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
	
	private final Appendable out;
	private int indent;
	
	private TAFWriter(Appendable out) {
		this.out = out;
	}
	
	private void begin(String type, Collection<?> inputs) throws IOException {
		writeIndent();
		out.append(type).append(' ');
		writeStringCollection(inputs);
		out.append(" {\n");
		indent++;
	}
	
	private void end() throws IOException {
		--indent;
		writeIndent();
		out.append("}\n");
	}
	
	private void writeIndent() throws IOException {
		for (int i = 0; i < indent; i++) {
			out.append('\t');
		}
	}
	
	private void beginState(String name, Set<String> options) throws IOException {
		writeIndent();
		out.append(name).append(' ');
		if (options != null && !options.isEmpty()) {
			out.append(options.toString()).append(' ');
		}
		out.append("{\n");
		indent++;
	}
	
	private void endState() throws IOException {
		--indent;
		writeIndent();
		out.append("}\n");
	}
	
	private void writeTransition(Collection<?> symbols, String target, Object output)
			throws IOException {
		writeIndent();
		writeStringCollection(symbols);
		if (output != null) {
			out.append(" / ").append(output.toString());
		}
		out.append(" -> ").append(target).append('\n');
	}
	
	private void writeStringCollection(Collection<?> symbols)
			throws IOException {
		if (symbols.isEmpty()) {
			out.append("{}");
		}
		else if (symbols.size() == 1) {
			StringUtil.enquoteIfNecessary(symbols.iterator().next().toString(), out, idPattern);
		}
		else {
			out.append('{');
			boolean first = true;
			for (Object sym : symbols) {
				if (first) {
					first = false;
				}
				else {
					out.append(',');
				}
				StringUtil.enquoteIfNecessary(sym.toString(), out, idPattern);
			}
			out.append('}');
		}
	}
	
	private <S,I,T> void doWriteAutomaton(
			UniversalDeterministicAutomaton<S, I, T,?,?> automaton,
			Collection<? extends I> inputs,
			String type,
			Function<S,? extends Collection<? extends String>> spExtractor)
			throws IOException {
		begin(type, inputs);
		{
			S init = automaton.getInitialState();
			StateIDs<S> ids = automaton.stateIDs();
			for (S state : automaton) {
				Set<String> options = new HashSet<>(spExtractor.apply(state));
				if (Objects.equals(init, state)) {
					options.add("initial");
				}
				int id = ids.getStateId(state);
				String name = "s" + id;
				
				beginState(name, options);
				{
					Map<Pair<S,Object>,List<I>> groupedTransitions =
						inputs.stream()
							.map(i -> new Pair<I,T>(i, automaton.getTransition(state, i)))
							.filter(p -> p.getSecond() != null)
							.collect(Collectors.groupingBy(
								p -> new Pair<S,Object>(
										automaton.getSuccessor(p.getSecond()),
										automaton.getTransitionProperty(p.getSecond())),
								Collectors.mapping(p -> p.getFirst(), Collectors.toList())
								));
					
					for (Map.Entry<Pair<S,Object>,List<I>> group : groupedTransitions.entrySet()) {
						S tgt = group.getKey().getFirst();
						int tgtId = ids.getStateId(tgt);
						String tgtName = "s" + tgtId;
						Object transProp = group.getKey().getSecond();
						writeTransition(group.getValue(), tgtName, transProp);
					}
				}
				endState();
			}
		}
		end();
	}
		
	
	public static <I> void writeAny(FiniteAlphabetAutomaton<?, I, ?> automaton, Appendable out)
			throws IOException {
		writeAny(automaton, automaton.getInputAlphabet(), out);
	}
	
	@SuppressWarnings("unchecked")
	public static <I> void writeAny(Automaton<?, I, ?> automaton, Collection<? extends I> inputs, Appendable out)
			throws IOException {
		if (automaton instanceof DFA) {
			writeDFA((DFA<?,I>) automaton, inputs, out);
		}
		else if (automaton instanceof MealyMachine) {
			writeMealy((MealyMachine<?,I,?,?>) automaton, inputs, out);
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	public static <S,I> void writeDFA(DFA<S,I> dfa, Collection<? extends I> inputs, Appendable out)
			throws IOException {
		TAFWriter writer = new TAFWriter(out);
		writer.doWriteAutomaton(dfa, inputs, "dfa",
				s -> dfa.isAccepting(s) ? Collections.singleton("accepting") : Collections.emptySet());
	}
	
	public static <I> void writeMealy(MealyMachine<?,I,?,?> mealy, Collection<? extends I> inputs, Appendable out)
			throws IOException {
		TAFWriter writer = new TAFWriter(out);
		writer.doWriteAutomaton(mealy, inputs, "mealy", s -> Collections.emptySet());
	}
	
	
	public static <I> void dfaToString(DFA<?,I> dfa, Collection<? extends I> inputs) {
		try {
			StringBuilder sb = new StringBuilder();
			writeDFA(dfa, inputs, sb);
		}
		catch (IOException ex) {
			throw new AssertionError();
		}
	}
	
	public static <I> void writeDFA(DFA<?,I> dfa, Collection<? extends I> inputs, File out)
			throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			writeDFA(dfa, inputs, bw);
		}
	}
	
	
	public static <I> void writeMealy(MealyMachine<?,I,?,?> mealy, Collection<? extends I> inputs, File out)
			throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			writeMealy(mealy, inputs, bw);
		}
	}
	
	public static <I> void mealyToString(MealyMachine<?,I,?,?> mealy, Collection<? extends I> inputs) {
		try {
			StringBuilder sb = new StringBuilder();
			writeMealy(mealy, inputs, sb);
		}
		catch (IOException ex) {
			throw new AssertionError();
		}
	}

}
