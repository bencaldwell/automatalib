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

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.automatalib.commons.util.IOUtil;


/**
 * Utility class to simplify operating the GraphVIZ "dot" utility. Please note that all
 * of the provided methods require GraphVIZ to be installed on the system, and that the
 * "dot" binary resides in the execution path.
 * 
 * @author Malte Isberner 
 *
 */
public class DOT {
	
	private static final Logger LOGGER = Logger.getLogger("automatalib.dotutil");
	
	private static final int MAX_WIDTH = 800;
	private static final int MAX_HEIGHT = 600;
	
	private static String dotExe = "dot";
	
	public static void setDotExe(String dotExe) {
		DOT.dotExe = dotExe;
	}
	
	public static boolean checkUsable() {
		try {
			Process p = executeDOTRaw("-V");
			
			int result = p.waitFor();
			if (result == 0) {
				return true;
			}
		}
		catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static Process executeDOTRaw(String... opts) throws IOException {
		String[] dotArgs = new String[1 + opts.length];
		dotArgs[0] = dotExe;
		System.arraycopy(opts, 0, dotArgs, 1, opts.length);
		Process dot = Runtime.getRuntime().exec(dotArgs);
		
		return dot;
	}
	
	public static Process executeDOT(String format, String ...additionalOpts) throws IOException {
		String[] dotArgs = new String[1 + additionalOpts.length];
		dotArgs[0] = "-T" + format;
		System.arraycopy(additionalOpts, 0, dotArgs, 1, additionalOpts.length);
		
		return executeDOTRaw(dotArgs);
	}
	
	
	/**
	 * Invokes the GraphVIZ DOT utility for rendering graphs.
	 * @param r the reader from which the GraphVIZ description is obtained.
	 * @param format the output format, as understood by the dot utility, e.g.,
	 * png, ps, ...
	 * @return an input stream from which the image data can be read.
	 * @throws IOException if reading from the specified reader fails.
	 */
	public static InputStream runDOT(Reader r, String format, String ...additionalOpts) throws IOException {
		Process dot = executeDOT(format, additionalOpts);
		
		OutputStream dotIn = dot.getOutputStream();
		Writer dotWriter = new OutputStreamWriter(dotIn);
		
		IOUtil.copy(r, dotWriter);
	
		try {
			IOUtil.skip(dot.getErrorStream());
		}
		catch(IOException e) {}
		
		return dot.getInputStream();
	}
	
	/**
	 * Invokes the DOT utility on a string.
	 * Convenience method, see {@link #runDOT(Reader, String, String...)}
	 */
	public static InputStream runDOT(String dotText, String format, String ...additionalOpts) throws IOException {
		StringReader sr = new StringReader(dotText);
		return runDOT(sr, format, additionalOpts);
	}
	
	/**
	 * Invokes the DOT utility on a file.
	 * Convenience method, see {@link #runDOT(Reader, String, String...)}.
	 */
	public static InputStream runDOT(File dotFile, String format, String ...additionalOpts) throws IOException {
		FileReader fr = new FileReader(dotFile);
		return runDOT(fr, format, additionalOpts);
	}
	
	/**
	 * Invokes the GraphVIZ DOT utility for rendering graphs, writing output
	 * to the specified file.
	 * @param r the reader from which the GraphVIZ description is read.
	 * @param format the output format to produce.
	 * @param out the file to which the output is written.
	 * @throws IOException if an I/O error occurs reading from the given input
	 * or writing to the output file.
	 */
	public static void runDOT(Reader r, String format, File out) throws IOException {
		Process dot = executeDOT(format, "-o" + out.getAbsolutePath());
		
		OutputStream dotIn = dot.getOutputStream();
		Writer dotWriter = new OutputStreamWriter(dotIn);
		
		IOUtil.copy(r, dotWriter);
		dot.getErrorStream().close();
		dot.getInputStream().close();
		try {
			dot.waitFor();
		}
		catch(InterruptedException ex) {
			LOGGER.warning("Interrupted while waiting for 'dot' process to exit." + ex);
		}
	}
	
	
	/**
	 * Invokes the DOT utility on a string, producing an output file.
	 * Convenience method, see {@link #runDOT(Reader, String, File)}.
	 */
	public static void runDOT(String dotText, String format, File out) throws IOException {
		runDOT(new StringReader(dotText), format, out);
	}
	
	/**
	 * Invokes the DOT utility on a file, producing an output file.
	 * Convenience method, see {@link #runDOT(Reader, String, File)}.
	 */
	public static void runDOT(File dotFile, String format, File out) throws IOException, FileNotFoundException {
		runDOT(new FileReader(dotFile), format, out);
	}
	
	/**
	 * Renders a GraphVIZ description, using an external program for displaying.
	 * The program is determined by the system's file type associations,
	 * using the {@link Desktop#open(File)} method. 
	 * @param r the reader from which the GraphVIZ description is read.
	 * @param format the output format, as understood by the dot utility, e.g.,
	 * png, ps, ... 
	 */
	public static void renderDOTExternal(Reader r, String format) {
		try {
			File image = File.createTempFile("dot", format);
			runDOT(r, format, image);
			Desktop.getDesktop().open(image);
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Error rendering DOT: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Renders a GraphVIZ description from a string, using an external program
	 * for displaying.
	 * Convenience method, see {@link #renderDOTExternal(Reader, String)}.
	 */
	public static void renderDOTExternal(String dotText, String format) {
		renderDOTExternal(new StringReader(dotText), format);
	}
	
	/**
	 * Renders a GraphVIZ description from a file, using an external program
	 * for displaying.
	 * Convenience method, see {@link #renderDOTExternal(Reader, String)}.
	 * @throws FileNotFoundException if the specified file was not found.
	 */
	public static void renderDOTExternal(File dotFile, String format) throws FileNotFoundException {
		renderDOTExternal(new FileReader(dotFile), format);
	}
	
	/**
	 * Renders a GraphVIZ description and displays it in a Swing window.
	 * @param r the reader from which the description is obtained.
	 * @param modal whether or not the dialog should be modal.
	 */
	public static void renderDOT(Reader r, boolean modal) {
		final DOTComponent cmp = createDOTComponent(r);
		if (cmp == null)
			return;

		final JDialog frame = new JDialog((Dialog) null, modal);
		JScrollPane scrollPane = new JScrollPane(cmp);
		frame.setContentPane(scrollPane);
		frame.setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
		frame.pack();
		JMenu menu = new JMenu("File");
		menu.add(cmp.getSavePngAction());
		menu.add(cmp.getSaveDotAction());
		menu.addSeparator();
		menu.add(new AbstractAction("Close") {
			private static final long serialVersionUID = -1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
					frame.setVisible(false);
			}
		});
	}
	
	
	/**
	 * Renders a GraphVIZ description from a string and displays it in a
	 * Swing window.
	 * Convenience method, see {@link #renderDOT(Reader, boolean)}.
	 */
	public static void renderDOT(String dotText, boolean modal) {
		renderDOT(new StringReader(dotText), modal);
	}
	
	/**
	 * Renders a GraphVIZ description from a string and displays it in a
	 * Swing window.
	 * Convenience method, see {@link #renderDOT(Reader, boolean)}.
	 * @throws FileNotFoundException if the specified file was not found.
	 */
	public static void renderDOT(File dotFile, boolean modal) throws FileNotFoundException {
		renderDOT(new FileReader(dotFile), modal);
	}	
	
	/**
	 * Reads a DOT description from a reader and returns the PNG rendering result as a
	 * {@link BufferedImage}.
	 * @param dotReader the reader from which to read the description
	 * @return the rendering result
	 * @throws IOException if reading from the reader fails, or the pipe to the
	 * DOT process breaks.
	 */
	public static BufferedImage renderDOTImage(Reader dotReader) throws IOException {
		InputStream pngIs = runDOT(dotReader, "png");
		BufferedImage img = ImageIO.read(pngIs);
		pngIs.close();
		
		return img;
	}
	
	/**
	 * Reads a DOT description from a string and returns the PNG rendering result as a
	 * {@link BufferedImage}.
	 * @param dotText the DOT description
	 * @return the rendering result
	 * @throws IOException if the pipe to the DOT process breaks.
	 */
	public static BufferedImage renderDOTImage(String dotText) throws IOException {
		return renderDOTImage(new StringReader(dotText));
	}
	
	/**
	 * Reads a DOT description from a file and returns the PNG rendering result as a
	 * {@link BufferedImage}.
	 * @param dotFile the file containing the DOT description
	 * @return the rendering result
	 * @throws IOException if reading from the file fails or the pipe to the DOT process breaks.
	 */
	public static BufferedImage renderDOTImage(File dotFile) throws IOException {
		return renderDOTImage(new BufferedReader(new FileReader(dotFile)));
	}
	
	/**
	 * Creates a {@link DOTComponent} that displays the result of rendering a DOT description
	 * read from a {@link Reader}.
	 * @param r the reader to read from
	 * @return the DOT component
	 */
	public static DOTComponent createDOTComponent(Reader r) {
		try {
			DOTComponent dc = new DOTComponent(r);
			return dc;
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Could not run DOT: " + e.getMessage(), "Failed to run DOT", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	/**
	 * Creates a Writer that can be used to write a DOT description to. Upon closing the writer,
	 * a window with the rendering result appears.
	 * @param modal whether or not this window is modal (if set to <tt>true</tt>, calls to
	 * {@link Writer#close()} will block.
	 * @return the writer
	 */
	public static Writer createDotWriter(final boolean modal) {
		return new StringWriter() {
			@Override
			public void close() throws IOException {
				renderDOT(getBuffer().toString(), modal);
				super.close();
			}
		};
	}

}
