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
package net.automatalib.commons.util.settings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import com.google.common.collect.Iterators;

public interface SettingsSource {
	public void loadSettings(Properties props);
	
	default public int getPriority() {
		return 0;
	}
	
	
	public static <S extends SettingsSource> Properties readSettings(Class<S> clazz) {
		Properties p = new Properties();
		readSettings(clazz, p);
		return p;
	}
	
	public static <S extends SettingsSource> void readSettings(Class<S> clazz, Properties p) {
		ServiceLoader<S> loader = ServiceLoader.load(clazz);
		List<S> sources = new ArrayList<>();
		Iterators.addAll(sources, loader.iterator());
		sources.sort(new Comparator<SettingsSource>() {
			@Override
			public int compare(SettingsSource a, SettingsSource b) {
				return a.getPriority() - b.getPriority();
			}
		});
		
		for (S source : sources) {
			source.loadSettings(p);
		}
	}
}