package fr.univmrs.tagc.GINsim.regulatoryGraph;

import java.util.ArrayList;
import java.util.List;

import org.ginsim.gui.GUIManager;

import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;

/**
 * Simple example showing how to create and fill a regulatory graph.
 * It is meant to be used as reminder for authors of import filters.
 * 
 * @author Aurelien Naldi
 */
public class LRGCreator {

	public static void main(String[] args) {
		// create a simple graph
		GsRegulatoryGraph lrg = new GsRegulatoryGraph();
		GsVertexAttributesReader vreader = lrg.getVertexAttributeReader();
		
		// add a few vertices
		GsRegulatoryVertex g0, g1, g2;
		g0 = lrg.addVertex();
		g1 = lrg.addVertex();
		g2 = lrg.addVertex();
		
		// change their position
		vreader.setVertex(g0);
		vreader.setPos(0, 0);
		vreader.setVertex(g1);
		vreader.setPos(30, 0);
		vreader.setVertex(g2);
		vreader.setPos(0, 100);

		// increase max value for g0
		// we have to give the lrg as argument for consistency checks and cascading events
		g0.setMaxValue((byte)2, lrg);
		
		// add positive and negative interactions
		lrg.addEdge(g0, g2, 1);
		lrg.addEdge(g1, g2, -1);
		
		// get an edge (interactiveAddEdge does not return it)
		GsRegulatoryMultiEdge me = lrg.getEdge(g0, g2);
		// change its threshold
		me.setMin(0, (byte)2);
		
		// create a logical parameter.
		// it takes a value and a list of interactions
		List edges = new ArrayList();
		edges.add(me.getEdge(0));  // interaction == one of the edges inside a MultiEdge
		GsLogicalParameter parameter = new GsLogicalParameter(edges, 1);
		
		// add the logical parameter on g2
		g2.addLogicalParameter(parameter, true);  // manual parameter, logical function will call this method with "false"
		
		// propose to display the new graph 
		GUIManager.getInstance().whatToDoWithGraph(lrg, null, true);
	}
}
