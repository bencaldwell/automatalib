/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.serialization.taf.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import net.automatalib.automata.FiniteAlphabetAutomaton;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;

/**
 * Facade for TAF parsing. This class provides several static methods to read TAF
 * descriptions for DFA and Mealy machines.
 * 
 * @author Malte Isberner
 */
public class TAFParser {
	
	public static CompactDFA<String> parseDFA(Reader reader, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		InternalTAFParser parser = new InternalTAFParser(reader);
		parser.setDiagnosticListener(listener);
		DefaultTAFBuilderDFA builder = new DefaultTAFBuilderDFA(parser);
		try {
			parser.dfa(builder);
		}
		catch (ParseException ex) {
			throw new TAFParseException(ex);
		}
		return builder.finish();
	}
	
	public static CompactDFA<String> parseDFA(InputStream stream, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseDFA(new InputStreamReader(stream), listener);
	}
	
	public static CompactDFA<String> parseDFA(File file, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			return parseDFA(r, listener);
		}
		catch (IOException ex) {
			throw new TAFParseException(ex);
		}
	}
	
	public static CompactDFA<String> parseDFA(String string, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseDFA(new StringReader(string), listener);
	}
	
	public static CompactMealy<String,String> parseMealy(Reader reader, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		InternalTAFParser parser = new InternalTAFParser(reader);
		parser.setDiagnosticListener(listener);
		DefaultTAFBuilderMealy builder = new DefaultTAFBuilderMealy(parser);
		try {
			parser.mealy(builder);
		}
		catch (ParseException ex) {
			throw new TAFParseException(ex);
		}
		return builder.finish();
	}
	
	public static CompactMealy<String,String> parseMealy(InputStream stream, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseMealy(new InputStreamReader(stream), listener);
	}
	
	public static CompactMealy<String,String> parseMealy(File file, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			return parseMealy(r, listener);
		}
		catch (IOException ex) {
			throw new TAFParseException(ex);
		}
	}
	
	public static CompactMealy<String,String> parseMealy(String string, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseMealy(new StringReader(string), listener);
	}
	
	
	
	
	public static FiniteAlphabetAutomaton<?,String,?>
	parseAny(Reader reader, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		InternalTAFParser parser = new InternalTAFParser(reader);
		parser.setDiagnosticListener(listener);
		try {
			Type type = parser.type();
			switch (type) {
			case DFA:
			{
				DefaultTAFBuilderDFA builder = new DefaultTAFBuilderDFA(parser);
				parser.dfaBody(builder);
				return builder.finish();
			}
			case MEALY:
			{
				DefaultTAFBuilderMealy builder = new DefaultTAFBuilderMealy(parser);
				parser.mealyBody(builder);
				return builder.finish();
			}
			}
		}
		catch (ParseException ex) {
			throw new TAFParseException(ex);
		}
		throw new IllegalArgumentException();
	}
	
	public static FiniteAlphabetAutomaton<?,String,?>
	parseAny(InputStream stream, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseAny(new InputStreamReader(stream), listener);
	}
	
	public static FiniteAlphabetAutomaton<?,String,?>
	parseAny(File file, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			return parseAny(r, listener);
		}
		catch (IOException ex) {
			throw new TAFParseException(ex);
		}
	}
	
	public static FiniteAlphabetAutomaton<?,String,?>
	parseAny(String string, TAFParseDiagnosticListener listener)
			throws TAFParseException {
		return parseDFA(new StringReader(string), listener);
	}
}
