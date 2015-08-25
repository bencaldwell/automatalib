/* Copyright (C) 2013-2014 TU Dortmund
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.abstractimpl.AbstractSymbol;

@ParametersAreNonnullByDefault
public class Symbol extends AbstractSymbol<Symbol> {
	@Nullable
	private final Object userObject;

	public Symbol(@Nullable Object userObject) {
		this.userObject = userObject;
	}
	
	@Override
	public int compareTo(@Nonnull Symbol o) {
		return getId() - o.getId();
	}
	
	@Nullable
	public Object getUserObject() {
		return userObject;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	@Nonnull
	public String toString() {
		return String.valueOf(userObject);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userObject == null) ? 0 : userObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Symbol other = (Symbol) obj;
		if (userObject == null) {
			if (other.userObject != null)
				return false;
		} else if (!userObject.equals(other.userObject))
			return false;
		return true;
	}
	
	
}
