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
package net.automatalib.examples.incremental;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.commons.dotutil.DOT;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class IncrementalDFAExample {
	private static final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
	
	public static void build(IncrementalDFABuilder<Character> incDfa) throws IOException {
		Word<Character> w1 = Word.fromString("abc");
		Word<Character> w2 = Word.fromString("ac");
		Word<Character> w3 = Word.fromString("acb");
		Word<Character> w4 = Word.epsilon();
		
		System.out.println("  Inserting " + w1 + " as accepted");
		incDfa.insert(w1, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w2 + " as rejected");
		incDfa.insert(w2, false);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w3 + " as accepted");
		incDfa.insert(w3, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
		
		System.out.println("  Inserting " + w4 + " as accepted");
		incDfa.insert(w4, true);
		try(Writer w = DOT.createDotWriter(true)) {
			GraphDOT.write(incDfa.asGraph(), w);
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Incremental construction using a Tree");
		IncrementalDFABuilder<Character> incDfaTree
			= new IncrementalDFATreeBuilder<>(alphabet);
		build(incDfaTree);
		
		System.out.println();
		
		System.out.println("Incremental construction using a DAG");
		IncrementalDFABuilder<Character> incDfaDag
			= new IncrementalDFADAGBuilder<>(alphabet);
		build(incDfaDag);
	}
}
