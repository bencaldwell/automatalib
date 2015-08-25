/* Copyright (C) 2015 TU Dortmund
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
package net.automatalib.serialization.saf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.serialization.saf.SAFSerialization.AutomatonType;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

class SAFInput {
	
	private static final AutomatonType[] types = SAFSerialization.AutomatonType.values();
	
	private final DataInput in;
	
	public SAFInput(DataInput in) {
		this.in = in;
	}
	
	public SAFInput(InputStream is) {
		this((DataInput)new DataInputStream(is));
	}
	
	public SAFInput(byte[] data) {
		this(new ByteArrayInputStream(data));
	}
	
	public SAFInput(File file) throws FileNotFoundException {
		this(new BufferedInputStream(new FileInputStream(file)));
	}
	
	
	public AutomatonType readHeader() throws IOException {
		byte[] header = new byte[4];
		in.readFully(header);
		if (header[0] != 'S' || header[1] != 'A' || header[2] != 'F') {
			throw new RuntimeException();
		}
		byte type = header[3];
		if (type < 0 || type >= types.length) {
			throw new RuntimeException();
		}
		return types[type];
	}
	
	private <S,I,TP> void decodeTransitionsDet(
			MutableAutomaton<S, I, ?, ?, TP> result,
			List<S> stateList,
			Alphabet<I> alphabet,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		int numStates = stateList.size();
		assert result.size() == numStates;
		
		int numInputs = alphabet.size();
		
		for (S state : stateList) {
			for (int j = 0; j < numInputs; j++) {
				int tgt = in.readInt();
				if (tgt != -1) {
					I sym = alphabet.getSymbol(j);
					S tgtState = stateList.get(tgt);
					TP prop = tpDecoder.readProperty(in);
					result.addTransition(state, sym, tgtState, prop);
				}
			}
		}
	}
	
	private <S,I,TP> void decodeTransitionsNondet(
			MutableAutomaton<S,I,?,?,TP> result,
			List<S> stateList,
			Alphabet<I> alphabet,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		int numStates = stateList.size();
		assert result.size() == numStates;
		
		int numInputs = alphabet.size();
		
		for (S state : stateList) {
			for (int j = 0; j < numInputs; j++) {
				int numTgts = in.readInt();
				I sym = alphabet.getSymbol(j);
				for (int k = 0; k < numTgts; k++) {
					int tgt = in.readInt();
					TP prop = tpDecoder.readProperty(in);
					S tgtState = stateList.get(tgt);
					result.addTransition(state, sym, tgtState, prop);
				}
			}
		}
	}
	
	
	private <S,SP> List<S> decodeStateProperties(
			MutableAutomaton<S, ?, ?, SP, ?> result,
			int numStates,
			BlockPropertyDecoder<? extends SP> decoder) throws IOException {
		List<S> stateList = new ArrayList<>(numStates);
		
		decoder.start(in);
		for (int i = 0; i < numStates; i++) {
			SP prop = decoder.readProperty(in);
			S state = result.addState(prop);
			stateList.add(state);
		}
		
		return stateList;
	}
	
	private <S,SP> List<S> decodeStatesDet(
			MutableAutomaton<S, ?, ?, SP, ?> result,
			int numStates,
			BlockPropertyDecoder<? extends SP> decoder) throws IOException {
		int initStateId = in.readInt();
		
		List<S> stateList = decodeStateProperties(result, numStates, decoder);
		
		S initState = stateList.get(initStateId);
		
		result.setInitial(initState, true);
		
		return stateList;
	}
	
	private <S,SP> List<S> decodeStatesNondet(
			MutableAutomaton<S,?,?,SP,?> result,
			int numStates,
			BlockPropertyDecoder<? extends SP> decoder) throws IOException {
		int[] initStates = readInts(in);
		
		List<S> stateList = decodeStateProperties(result, numStates, decoder);
		
		for (int initId : initStates) {
			S initState = stateList.get(initId);
			result.setInitial(initState, true);
		}
		
		return stateList;
	}
	
	private <S,I,SP,TP> List<S> decodeBodyDet(
			MutableAutomaton<S, I, ?, SP, TP> result,
			Alphabet<I> alphabet,
			int numStates,
			BlockPropertyDecoder<? extends SP> spDecoder,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		
		List<S> stateList = decodeStatesDet(result, numStates, spDecoder);
		decodeTransitionsDet(result, stateList, alphabet, tpDecoder);
		
		return stateList;
	}
	
	private <S,I,SP,TP> List<S> decodeBodyNondet(
			MutableAutomaton<S, I, ?, SP, TP> result,
			Alphabet<I> alphabet,
			int numStates,
			BlockPropertyDecoder<? extends SP> spDecoder,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		
		List<S> stateList = decodeStatesNondet(result, numStates, spDecoder);
		decodeTransitionsNondet(result, stateList, alphabet, tpDecoder);
		
		return stateList;
	}
	
	private static int[] readInts(DataInput in) throws IOException {
		int n = in.readInt();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = in.readInt();
		}
		return result;
	}
	
	private <I,SP,TP,A extends MutableAutomaton<?,I,?,SP,TP>> A readAutomatonBody(
			Alphabet<I> alphabet,
			boolean deterministic,
			AutomatonCreator<? extends A, I> creator,
			BlockPropertyDecoder<? extends SP> spDecoder,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		int numStates = in.readInt();
		A result = creator.createAutomaton(alphabet, numStates);
		
		// this cast is required ..
		MutableAutomaton<?,I,?,SP,TP> result_ = (MutableAutomaton<?,I,?,SP,TP>)result;
		if (deterministic) {
			decodeBodyDet(result_, alphabet, numStates, spDecoder, tpDecoder);
		}
		else {
			decodeBodyNondet(result_, alphabet, numStates, spDecoder, tpDecoder);
		}
		
		return result;
	}
	
	public <I,SP,TP,A extends MutableAutomaton<?,I,?,SP,TP>> A readAutomaton(
			AutomatonType expectedType,
			Alphabet<I> alphabet,
			AutomatonCreator<? extends A, I> creator,
			BlockPropertyDecoder<? extends SP> spDecoder,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		AutomatonType type = readHeader();
		if (type != expectedType) {
			throw new RuntimeException();
		}
		int alphabetSize = in.readInt();
		if (alphabetSize != alphabet.size()) {
			throw new RuntimeException();
		}
		return readAutomatonBody(alphabet, type.isDeterministic(), creator, spDecoder, tpDecoder);
	}
	
	public <SP,TP,A extends MutableAutomaton<?, Integer, ?, SP, TP>> A readNativeAutomaton(
			AutomatonType expectedType,
			AutomatonCreator<? extends A, Integer> creator,
			BlockPropertyDecoder<? extends SP> spDecoder,
			SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
		AutomatonType type = readHeader();
		if (type != expectedType) {
			throw new RuntimeException();
		}
		int alphabetSize = in.readInt();
		if (alphabetSize <= 0) {
			throw new RuntimeException();
		}
		Alphabet<Integer> alphabet = Alphabets.integers(0, alphabetSize - 1);
		return readAutomatonBody(alphabet, type.isDeterministic(), creator, spDecoder, tpDecoder);
	}
	
	public <I> CompactDFA<I> readDFA(Alphabet<I> alphabet) throws IOException {
		return readAutomaton(
				AutomatonType.DFA,
				alphabet,
				new CompactDFA.Creator<>(),
				new AcceptanceDecoder(),
				in -> null);
	}
	
	public CompactDFA<Integer> readNativeDFA() throws IOException {
		return readNativeAutomaton(
				AutomatonType.DFA,
				new CompactDFA.Creator<>(),
				new AcceptanceDecoder(),
				in -> null);
	}
	
	public <I> CompactNFA<I> readNFA(Alphabet<I> alphabet) throws IOException {
		return readAutomaton(
				AutomatonType.NFA,
				alphabet,
				new CompactNFA.Creator<>(),
				new AcceptanceDecoder(),
				in -> null);
	}
	
	public CompactNFA<Integer> readNativeNFA() throws IOException {
		return readNativeAutomaton(
				AutomatonType.NFA,
				new CompactNFA.Creator<>(),
				new AcceptanceDecoder(),
				in -> null);
	}
}
