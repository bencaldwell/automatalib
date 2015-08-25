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
package net.automatalib.commons.util.lib;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class PlatformProperties {
	
	private static final Logger LOG = Logger.getLogger(PlatformProperties.class.getName());
	
	public static String OS_NAME;
	public static String OS_ARCH;
	public static String OS_VERSION;
	
	static {
		Properties aliases = new Properties();
		try(InputStream is = PlatformProperties.class.getResourceAsStream("/platform-aliases.properties")) {
			aliases.load(is);
		}
		catch (Exception ex) {
			LOG.warning("Could not load platform aliases file: " + ex.getMessage());
			LOG.warning("You may experience issues with the resolution of native libraries.");
		}
		
		String osName = System.getProperty("os.name").toLowerCase().replace(' ', '_').replace('/', '_');
		OS_NAME = aliases.getProperty("os." + osName, osName);
		
		String osArch = System.getProperty("os.arch").toLowerCase().replace(' ', '_').replace('/', '_');
		OS_ARCH = aliases.getProperty("arch." + osArch, osArch);
		
		OS_VERSION = System.getProperty("os.version");
	}
	
	private PlatformProperties() {
		throw new AssertionError();
	}
}
