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
package net.automatalib.graphs.base.compact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.concepts.NodeIDs;

public abstract class AbstractCompactGraph<E extends CompactEdge<EP>,NP, EP>
		implements MutableGraph<Integer, E, NP, EP>, NodeIDs<Integer> {

	protected int size;
	protected final ResizingObjectArray edges;
	
	public AbstractCompactGraph() {
		this.size = 0;
		this.edges = new ResizingObjectArray();
	}
	
	public AbstractCompactGraph(int initialCapacity) {
		this.edges = new ResizingObjectArray(initialCapacity);
	}

	@SuppressWarnings("unchecked")
	protected List<E> getOutEdgeList(int node) {
		return (List<E>)edges.array[node];
	}

	@Override
	public Collection<Integer> getNodes() {
		return CollectionsUtil.intRange(0, size);
	}

	@Override
	public NodeIDs<Integer> nodeIDs() {
		return this;
	}

	@Override
	public Collection<E> getOutgoingEdges(Integer node) {
		return getOutgoingEdges(node.intValue());
	}

	public Collection<E> getOutgoingEdges(int node) {
		List<E> edgeList = getOutEdgeList(node);
		return Collections.unmodifiableCollection(edgeList);
	}

	@Override
	public Integer getTarget(E edge) {
		return Integer.valueOf(edge.getTarget());
	}

	@Override
	public Integer addNode(NP properties) {
		return Integer.valueOf(addIntNode(properties));
	}

	public int addIntNode() {
		return addIntNode(null);
	}

	public int addIntNode(NP properties) {
		edges.ensureCapacity(size + 1);
		edges.array[size] = new ArrayList<CompactEdge<EP>>();
		int n = size++;
		setNodeProperty(n, properties);
		return n;
	}

	@Override
	public E connect(Integer source, Integer target, EP properties) {
		return connect(source.intValue(), target.intValue(), properties);
	}

	public E connect(int source, int target, EP property) {
		E edge = createEdge(source, target, property);
		List<E> edges = getOutEdgeList(source);
		edge.outIndex = edges.size();
		edges.add(edge);
		return edge;
	}

	public CompactEdge<EP> connect(int source, int target) {
		return connect(source, target, null);
	}

	@Override
	public int getNodeId(Integer node) {
		return node.intValue();
	}

	@Override
	public Integer getNode(int id) {
		return Integer.valueOf(id);
	}
	
	public abstract NP getNodeProperties(int node);
	
	public abstract void setNodeProperty(int node, NP property);
	
	
	/* (non-Javadoc)
	 * @see net.automatalib.graphs.MutableGraph#setNodeProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setNodeProperty(Integer node, NP property) {
		setNodeProperty(node.intValue(), property);
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.MutableGraph#setEdgeProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setEdgeProperty(E edge, EP property) {
		edge.setProperty(property);
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.UniversalIndefiniteGraph#getNodeProperties(java.lang.Object)
	 */
	@Override
	public NP getNodeProperty(Integer node) {
		return getNodeProperties(node.intValue());
	}
	
	@Override
	public EP getEdgeProperty(E edge) {
		return edge.getProperty();
	}

	protected abstract E createEdge(int source, int target, EP property);

}