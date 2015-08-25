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
package net.automatalib.words.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.abstractimpl.AbstractAlphabet;


/**
 * A simple alphabet implementation, that does not impose any restriction on the input
 * symbol class. However, the id lookup for a symbol might be slightly slower.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol type
 */
public class SimpleAlphabet<I> extends AbstractAlphabet<I> implements GrowingAlphabet<I> {
	
	@Nonnull
	private final List<I> symbols;;
	
	@Nonnull
//	private final TObjectIntMap<I> indexMap = new TObjectIntHashMap<I>(10, 0.75f, -1);
	private final Map<I,Integer> indexMap = new HashMap<>(); // TODO: replace by primitive specialization
	
	public SimpleAlphabet() {
		this.symbols = new ArrayList<>();
	}
	
	public SimpleAlphabet(Collection<? extends I> symbols) {
		this.symbols = new ArrayList<>(symbols);
		int i = 0;
		for (I sym : this.symbols) {
			indexMap.put(sym, i++);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(I a) {
		int s = size();
		int idx = addSymbol(a);
		if(idx != s)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.GrowingAlphabet#addSymbol(java.lang.Object)
	 */
	@Override
	public int addSymbol(I a) {
//		int idx = indexMap.get(a);
//		if(idx != -1)
		Integer idx = indexMap.get(a); // TODO: replace by primitive specialization
		if (idx != null) {
			return idx;
		}
		idx = size();
		symbols.add(a);
		indexMap.put(a, idx);
		return idx;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<I> iterator() {
		return Collections.unmodifiableList(symbols).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return symbols.size();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbol(int)
	 */
	@Override
	public I getSymbol(int index) {
		return symbols.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbolIndex(java.lang.Object)
	 */
	@Override
	public int getSymbolIndex(I symbol) {
		return indexMap.get(symbol);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public I get(int index) {
		return getSymbol(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(I o1, I o2) {
		return indexMap.get(o1) - indexMap.get(o2);
	}
	
	@Override
	public boolean containsSymbol(I symbol) {
		return indexMap.containsKey(symbol);
	}

}
