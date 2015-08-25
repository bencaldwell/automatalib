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
package net.automatalib.commons.dotutil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

final class PlottedGraph {
	private String name;
	private String dotText;
	private BufferedImage image;
	
	
	public PlottedGraph(String name, Reader dotText) throws IOException {
		this.name = name;
		
		StringBuilder sb = new StringBuilder();
		
		char[] buf = new char[1024];
		int len;
		while((len = dotText.read(buf)) != -1)
			sb.append(buf, 0, len);
		
		try {
			dotText.close();
		}
		catch(IOException e) {}
		
		updateDOTText(sb.toString());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public String getDOTText() {
		return dotText;
	}
	
	public boolean updateDOTText(String dotText) {
		try {
			InputStream pngIs = DOT.runDOT(dotText, "png");
			try {
				BufferedImage img = ImageIO.read(pngIs);
				this.image = img;
			}
			finally {
				pngIs.close();
			}
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to invoke the DOT command!", "Failure rendering graph", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		this.dotText = dotText;
		return true;
	}

	public void saveDot(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			fw.write(dotText);
		}
		finally {
			fw.close();
		}
	}
	
	public void savePng(File file) throws IOException {
		ImageIO.write(this.image, "png", file);
	}
}
