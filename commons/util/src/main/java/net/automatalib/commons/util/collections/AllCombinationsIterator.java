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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

final class AllCombinationsIterator<T> implements Iterator<List<T>> {
	private final Iterable<T>[] iterables;
	private final Iterator<T>[] iterators;
	private final List<T> current;
	private boolean first = true;
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public AllCombinationsIterator(Iterable<T> ...iterables) {
		this.iterables = iterables;
		this.iterators = new Iterator[iterables.length];
		this.current = new ArrayList<T>(iterables.length);
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterables[i].iterator();
			this.iterators[i] = it;
			this.current.add(it.next());
		}	
	}

	@Override
	public boolean hasNext() {
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			if(it == null || it.hasNext())
				return true;
		}
		return false;
	}

	@Override
	public List<T> next() {
		if(first) {
			first = false;
			return current;
		}
		
		for(int i = 0; i < iterators.length; i++) {
			Iterator<T> it = iterators[i];
			
			if(iterators[i].hasNext()) {
				current.set(i, it.next());
				return current;
			}
			
			iterators[i] = it = iterables[i].iterator();
			current.set(i, it.next());
		}
		
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
}
