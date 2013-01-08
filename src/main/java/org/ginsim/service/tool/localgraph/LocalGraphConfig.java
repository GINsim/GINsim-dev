package org.ginsim.service.tool.localgraph;

import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;

public class LocalGraphConfig {

	private RegulatoryGraph graph;
	private DynamicGraph dyn;
//	private List<byte[]> states;

	public LocalGraphConfig(RegulatoryGraph graph, DynamicGraph dyn) {
		this.graph = graph;
		this.dyn = dyn;
	}

//	public void setStates(List<byte[]> states) {
//		this.states = states;
//	}

	public RegulatoryGraph getGraph() {
		return this.graph;
	}

	public DynamicGraph getDynamic() {
		return this.dyn;
	}
	
//	public List<byte[]> getStates() {
//		return this.states;
//	}
}
