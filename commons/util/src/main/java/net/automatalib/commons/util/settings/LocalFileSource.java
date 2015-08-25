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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class LocalFileSource implements SettingsSource {

	private final File file;
	
	protected LocalFileSource(File file) {
		this.file = file;
	}
	
	protected LocalFileSource(String fileName) {
		this(new File(fileName));
	}
	
	@Override
	public int getPriority() {
		// This is directly under user control, so it should have the second-highest possible
		// priority (to be overridden only by system properties)
		return Integer.MAX_VALUE - 1;
	}

	@Override
	public void loadSettings(Properties properties) {
		if (!file.exists()) {
			return;
		}
		
		Logger log = Logger.getLogger(getClass().getName());
		
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			properties.load(r);
		}
		catch(IOException ex) {
			log.warning("Could not read properties file " + file.getAbsolutePath() + ": " + ex.getMessage());
		}
	}
	
	
}
