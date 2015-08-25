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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.SerializationProvider;
import net.automatalib.words.Alphabet;


public class SAFSerialization implements SerializationProvider {
	
	enum AutomatonType {
		DFA(true),
		NFA(false),
		MEALY(true);
		
		private final boolean deterministic;
		
		public boolean isDeterministic() {
			return deterministic;
		}
		private AutomatonType(boolean deterministic) {
			this.deterministic = deterministic;
		}
	}
	
	private static final SAFSerialization INSTANCE = new SAFSerialization();
	
	public static SAFSerialization getInstance() {
		return INSTANCE;
	}

	private SAFSerialization() {
		
	}

	@Override
	public CompactDFA<Integer> readGenericDFA(InputStream is)
			throws IOException {
		is = IOUtil.asUncompressedInputStream(is);
		SAFInput in = new SAFInput(is);
		return in.readNativeDFA();
	}

	@Override
	public <I> void writeDFA(DFA<?, I> dfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompactNFA<Integer> readGenericNFA(InputStream is)
			throws IOException {
		is = IOUtil.asUncompressedInputStream(is);
		SAFInput in = new SAFInput(is);
		return in.readNativeNFA();
	}

	@Override
	public <I> void writeNFA(NFA<?, I> nfa, Alphabet<I> alphabet,
			OutputStream os) throws IOException {
		throw new UnsupportedOperationException();
	}

}
