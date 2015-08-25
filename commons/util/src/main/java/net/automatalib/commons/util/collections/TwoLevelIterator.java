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

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class TwoLevelIterator<L1, L2, O> implements Iterator<O> {
	
	private final Iterator<? extends L1> l1Iterator;
	private L1 l1Object;
	private Iterator<L2> l2Iterator;
	
	public TwoLevelIterator(Iterator<? extends L1> l1Iterator) {
		this.l1Iterator = l1Iterator;
		this.l2Iterator = null;
	}
	
	protected abstract Iterator<L2> l2Iterator(L1 l1Object);
	
	protected abstract O combine(L1 l1Object, L2 l2Object);
	
	protected void nextL1() {
		this.l2Iterator = null;
	}
	
	protected boolean advance() {
		while(l2Iterator == null || !l2Iterator.hasNext()) {
			if(!l1Iterator.hasNext())
				return false;
			this.l1Object = l1Iterator.next();
			this.l2Iterator = l2Iterator(this.l1Object);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if(l2Iterator != null && l2Iterator.hasNext())
			return true;
		return advance();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public O next() {
		if(l2Iterator == null || !l2Iterator.hasNext()) {
			if(!advance())
				throw new NoSuchElementException();
		}
		return combine(l1Object, l2Iterator.next());	
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		l2Iterator.remove();
	}
	
	
	
}
