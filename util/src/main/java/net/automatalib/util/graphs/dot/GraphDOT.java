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
package net.automatalib.util.graphs.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.AutomataLibSettings;
import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.commons.util.strings.StringUtil;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UndirectedGraph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.automata.Automata;


/**
 * Methods for rendering a {@link Graph} or {@link Automaton} in the GraphVIZ DOT format.
 * <p>
 * This class does not take care of actually processing the generated DOT data. For this,
 * please take a look at the <tt>automata-commons-dotutil</tt> artifact.
 * 
 * @author Malte Isberner 
 *
 */
public abstract class GraphDOT {
	
	
	static {
		AutomataLibSettings settings = AutomataLibSettings.getInstance();
		
		String dotExePath = settings.getProperty("dot.exe.dir");
		String dotExeName = settings.getProperty("dot.exe.name", "dot");
		
		String dotExe = dotExeName;
		if (dotExePath != null) {
			Path dotBasePath = FileSystems.getDefault().getPath(dotExePath);
			Path resolvedDotPath = dotBasePath.resolve(dotExeName);
			dotExe = resolvedDotPath.toString();
		}
		
		DOT.setDotExe(dotExe);
	}
	
	public static boolean isDotUsable() {
		return DOT.checkUsable();
	}
	
	
	public static <N,E> void write(GraphViewable gv, Appendable a) throws IOException {
		Graph<?,?> graph = gv.graphView();
		write(graph, a);
	}
	
	/**
	 * Renders a {@link Graph} in the GraphVIZ DOT format. 
	 * @param graph the graph to render
	 * @param a the appendable to write to.
	 * @throws IOException if writing to <tt>a</tt> fails. 
	 */
	@SafeVarargs
	public static <N,E> void write(Graph<N, E> graph,
			Appendable a, GraphDOTHelper<N,? super E> ...additionalHelpers) throws IOException {
		GraphDOTHelper<N,? super E> helper = graph.getGraphDOTHelper();
		writeRaw(graph, helper, a, additionalHelpers);
	}
	
	/**
	 * Renders an {@link Automaton} in the GraphVIZ DOT format.
	 * 
	 * @param automaton the automaton to render.
	 * @param helper the helper to use for rendering
	 * @param inputAlphabet the input alphabet to consider
	 * @param a the appendable to write to.
	 * @throws IOException if writing to <tt>a</tt> fails.
	 */
	@SafeVarargs
	public static <S,I,T> void write(Automaton<S,I,T> automaton,
			GraphDOTHelper<S, ? super TransitionEdge<I,T>> helper,
			Collection<? extends I> inputAlphabet,
			Appendable a, GraphDOTHelper<S,? super TransitionEdge<I,T>> ...additionalHelpers) throws IOException {
		Graph<S,TransitionEdge<I,T>> ag = Automata.asGraph(automaton, inputAlphabet);
		writeRaw(ag, helper, a, additionalHelpers);
	}
	
	/**
	 * Renders an {@link Automaton} in the GraphVIZ DOT format.
	 * 
	 * @param automaton the automaton to render.
	 * @param inputAlphabet the input alphabet to consider
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	@SafeVarargs
	public static <S,I,T> void write(Automaton<S,I,T> automaton,
			Collection<? extends I> inputAlphabet,
			Appendable a, GraphDOTHelper<S,? super TransitionEdge<I,T>> ...additionalHelpers) throws IOException {
		
		write(automaton.transitionGraphView(inputAlphabet), a, additionalHelpers);
	}
	
	@SafeVarargs
	public static <N,E> void writeRaw(Graph<N,E> graph, GraphDOTHelper<N, ? super E> helper, Appendable a, GraphDOTHelper<N, ? super E> ...additionalHelpers) throws IOException {
		List<GraphDOTHelper<N,? super E>> helpers = new ArrayList<>(additionalHelpers.length + 1);
		helpers.add(helper);
		helpers.addAll(Arrays.asList(additionalHelpers));
		
		writeRaw(graph, a, helpers);
	}
	
	public static <N,E> void writeRaw(Graph<N,E> graph, Appendable a, List<GraphDOTHelper<N,? super E>> helpers) throws IOException {
		AggregateDOTHelper<N, E> aggHelper = new AggregateDOTHelper<>(helpers);
		writeRaw(graph, aggHelper, a);
	}
	
	/**
	 * Renders a {@link Graph} in the GraphVIZ DOT format.
	 * 
	 * @param graph the graph to render
	 * @param dotHelper the helper to use for rendering
	 * @param a the appendable to write to
	 * @throws IOException if writing to <tt>a</tt> fails
	 */
	public static <N,E> void writeRaw(Graph<N, E> graph,
			GraphDOTHelper<N,? super E> dotHelper,
			Appendable a) throws IOException {
		
		if(dotHelper == null)
			dotHelper = new DefaultDOTHelper<N, E>();
		
		boolean directed = true;
		if(graph instanceof UndirectedGraph)
			directed = false;
		
		if(directed)
			a.append("di");
		a.append("graph g {\n");
		

		Map<String,String> props = new HashMap<>();
		
		dotHelper.getGlobalNodeProperties(props);
		if(!props.isEmpty()) {
			a.append('\t').append("node");
			appendParams(props, a);
			a.append(";\n");
		}
		
		props.clear();
		dotHelper.getGlobalEdgeProperties(props);
		if(!props.isEmpty()) {
			a.append('\t').append("edge");
			appendParams(props, a);
			a.append(";\n");
		}
		
		
		dotHelper.writePreamble(a);
		a.append('\n');
		
		
		MutableMapping<N,String> nodeNames = graph.createStaticNodeMapping();
		
		int i = 0;
		
		for(N node : graph) {
			props.clear();
			if(!dotHelper.getNodeProperties(node, props))
				continue;
			String id = "s" + i++;
			a.append('\t').append(id);
			appendParams(props, a);
			a.append(";\n");
			nodeNames.put(node, id);
		}
		
		for(N node : graph) {
			String srcId = nodeNames.get(node);
			if(srcId == null)
				continue;
			Collection<? extends E> outEdges = graph.getOutgoingEdges(node);
			if(outEdges.isEmpty())
				continue;
			for(E e : outEdges) {
				N tgt = graph.getTarget(e);
				String tgtId = nodeNames.get(tgt);
				if(tgtId == null)
					continue;
				
				if(!directed && tgtId.compareTo(srcId) < 0)
					continue;
				
				props.clear();
				if(!dotHelper.getEdgeProperties(node, e, tgt, props))
					continue;
				
				a.append('\t').append(srcId).append(' ');
				if(directed)
					a.append("-> ");
				else
					a.append("-- ");
				a.append(tgtId);
				appendParams(props, a);
				a.append(";\n");
			}
		}
		
		a.append('\n');
		dotHelper.writePostamble(nodeNames, a);
		a.append("}\n");
		if (a instanceof Flushable) {
			((Flushable) a).flush();
		}
	}
	
	public static <N,E> void writeToFileRaw(Graph<N,E> graph,
			GraphDOTHelper<N,E> dotHelper,
			File file) throws IOException {
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writeRaw(graph, dotHelper, writer);
		}
	}
	
	
	private static void appendParams(Map<String,String> params, Appendable a)
			throws IOException {
		if(params == null || params.isEmpty())
			return;
		a.append(" [");
		boolean first = true;
		for(Map.Entry<String,String> e : params.entrySet()) {
			if(first)
				first = false;
			else
				a.append(' ');
			String key = e.getKey();
			String value = e.getValue();
			a.append(e.getKey()).append("=");
			// HTML labels have to be enclosed in <> instead of ""
			if(key.equals(GraphDOTHelper.CommonAttrs.LABEL) && value.toUpperCase().startsWith("<HTML>"))
				a.append('<').append(value.substring(6)).append('>');
			else
				StringUtil.enquote(e.getValue(), a);
		}
		a.append(']');
	}
}
