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
package net.automatalib.incremental.mealy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.words.Word;

public interface IncrementalMealyBuilder<I, O> extends IncrementalConstruction<MealyMachine<?,I,?,O>, I> {

	public static interface GraphView<I, O, N, E> extends Graph<N, E> {
		@Nullable
		public I getInputSymbol(@Nonnull E edge);
		@Nullable
		public O getOutputSymbol(@Nonnull E edge);
		@Nonnull
		public N getInitialNode();
	}
	
	public Word<O> lookup(Word<? extends I> inputWord);
	public boolean lookup(Word<? extends I> inputWord, List<? super O> output);
	public void insert(Word<? extends I> inputWord, Word<? extends O> outputWord) throws ConflictException;
	
	@Override
	public MealyTransitionSystem<?,I,?,O> asTransitionSystem();
	
	@Override
	public GraphView<I,O,?,?> asGraph();
}
