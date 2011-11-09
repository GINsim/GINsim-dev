package fr.univmrs.tagc.GINsim.reg2dyn.helpers;

import org.ginsim.graph.Graph;

import fr.univmrs.tagc.GINsim.dynamicGraph.GsDynamicGraph;
import fr.univmrs.tagc.GINsim.dynamicGraph.GsDynamicNode;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.GINsim.reg2dyn.GsSimulationParameters;
import fr.univmrs.tagc.GINsim.reg2dyn.SimulationQueuedState;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsGenericRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;


/**
 * This is the SimulationHelper used for the simulation of STG.
 * @author duncan
 *
 */
public class GsSTGSimulationHelper extends SimulationHelper {
	protected GsDynamicNode node;
	protected GsDynamicGraph stateTransitionGraph;
	protected GsVertexAttributesReader vreader;
	
	public GsSTGSimulationHelper(GsGenericRegulatoryGraph regGraph, GsSimulationParameters params) {
		stateTransitionGraph = new GsDynamicGraph(params.nodeOrder);
		if (regGraph instanceof Graph) {
			stateTransitionGraph.setAssociatedGraph((GsRegulatoryGraph)regGraph);
		}
        vreader = stateTransitionGraph.getVertexAttributeReader();
	    vreader.setDefaultVertexSize(5+10*params.nodeOrder.size(), 25);
        // add some default comments to the state transition graph
        stateTransitionGraph.getAnnotation().setComment(params.getDescr()+"\n");
	}

	public boolean addNode(SimulationQueuedState item) {
		node = new GsDynamicNode(item.state);
		boolean isnew = stateTransitionGraph.addVertex(node);
		if (item.previous != null) {
			stateTransitionGraph.addEdge((GsDynamicNode)item.previous, node, item.multiple);
		}
		return isnew;
	}

	public Graph endSimulation() {
		
		return stateTransitionGraph;
	}

	public void setStable() {
		node.setStable(true, vreader);
	}

	public Graph getRegulatoryGraph() {
		
		return this.stateTransitionGraph.getAssociatedGraph();
	}
	
	public Graph getDynamicGraph() {
		
		return this.stateTransitionGraph;
	}

	public Object getNode() {
		return node;
	}
	
	public void setNode(Object node) {
		this.node = (GsDynamicNode) node;
	}
}

