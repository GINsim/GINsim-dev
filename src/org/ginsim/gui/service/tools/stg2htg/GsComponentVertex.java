package org.ginsim.gui.service.tools.stg2htg;

import java.util.HashSet;
import java.util.Vector;

import org.ginsim.graph.reducedgraph.GsNodeReducedData;


public class GsComponentVertex extends GsNodeReducedData {

	private HashSet sigma;

	public GsComponentVertex(String id, Vector content, Object sigma) {
		super(id, content);
		this.sigma = (HashSet) sigma;
	}

	public GsComponentVertex(GsNodeReducedData scc, Object sigma) {
		super(scc.getId(), scc.getContent());
		this.sigma = (HashSet)sigma;
	}
	
	public GsComponentVertex(GsNodeReducedData scc, Object sigma, String id) {
		super(id, scc.getContent());
		this.sigma = (HashSet)sigma;
	}
	
	public HashSet getSigma() {
		return sigma;
	}

}
