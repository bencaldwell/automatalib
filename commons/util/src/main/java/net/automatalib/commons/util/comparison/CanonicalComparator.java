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
package net.automatalib.commons.util.comparison;

import java.util.Comparator;
import java.util.List;

final class CanonicalComparator<T extends List<? extends U>,U> implements Comparator<T> {
	
	private final Comparator<? super U> elemComparator;
	
	public CanonicalComparator(Comparator<? super U> elemComparator) {
		this.elemComparator = elemComparator;
	}

	@Override
	public int compare(T o1, T o2) {
		return CmpUtil.canonicalCompare(o1, o2, elemComparator);
	}

}
