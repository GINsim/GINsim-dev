package org.ginsim.service.tool.connectivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.reducedgraph.ReducedGraph;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.NodeShape;
import org.ginsim.core.notification.NotificationManager;



/**
 * the class with the algorithms for strongest connected component
 */
public class ConnectivityAlgo implements Runnable {
	public static final String COMPUTATION_DONE_MESSAGE = "Connectivity computation done";
	private Graph graph;
	private ConnectivityResult algoResult;
	private boolean cancel;

	/**
	 * get ready to run.
	 *
	 * @param graph
	 */
	public ConnectivityResult configure( Graph graph) {
		this.graph = graph;
		this.cancel = false;
		this.algoResult = new ConnectivityResult();
		return algoResult;
	}

	public void run() {
		List<NodeReducedData> components = getStronglyConnectedComponents();
		algoResult.setComponents(components);
		algoResult.algoIsComputed();
		NotificationManager.getManager().publishInformation(algoResult, COMPUTATION_DONE_MESSAGE);
	}

	/**
	 * Get the Strongly Connected Components from the backend and add them with a proper name in a list of NodeReducedData
	 * @return the list of components
	 */
	private List<NodeReducedData> getStronglyConnectedComponents() {
		Collection<Collection<?>> jcp = graph.getStronglyConnectedComponents();
		List<NodeReducedData> components = new ArrayList<NodeReducedData>(jcp.size());
		int id = 0;
		for (Collection<?> set: jcp) {
			String sid;
			if (set.size() == 1) {
				sid = null;
			} else {
				sid = "cc-"+id++;
			}
			NodeReducedData node = new NodeReducedData(sid, set);
			components.add(node);
		}
		return components;
	}

	/**
	 * Construct a reducedGraph from a list of components
	 * @param components
	 * @return the reducedGraph
	 */
	private ReducedGraph constructGraph(List<NodeReducedData> components) {
		ReducedGraph reducedGraph = GraphManager.getInstance().getNewGraph( ReducedGraph.class, (Graph)graph);
		HashMap<Object, NodeReducedData> nodeParentSCC = new HashMap<Object, NodeReducedData>(); //Map the a node to its parent SCC

		for (NodeReducedData component : components) {		//For each component
			reducedGraph.addNode(component);
			for (Object node : component.getContent()) {	//  for each nodes in the component
				nodeParentSCC.put(node, component);		//     add the node in the map nodeParentSCC as a key, with the current SCC node as value

			}
			if (cancel) return null;
		}

		for (NodeReducedData component : components) {		//For each component
			for (Object node : component.getContent()) {	//  for each nodes in the component
				Collection<Edge<?>> outgoingEdges = graph.getOutgoingEdges(node);
				for (Edge edge: outgoingEdges) {									//    for each edge outgoing from this node
					Object targetNode = edge.getTarget();
					NodeReducedData targetParent = nodeParentSCC.get(targetNode);
					if (nodeParentSCC.get(targetNode) != component) {			//      if the target of the edge is not in the SCC
						reducedGraph.addEdge(component, targetParent);
					}
					if (cancel) return null;
				}
			}
			if (cancel) return null;
		}

		NodeAttributesReader vreader = reducedGraph.getNodeAttributeReader();
		for (NodeReducedData component : components) {				//For each component
            if (graph.getOutgoingEdges(component).size() == 0) {	//  set the node's shape to ellipse if the node has no outgoing edges (is terminal).
            	vreader.setNode(component);
                vreader.setShape(NodeShape.ELLIPSE);
            }
            if (cancel) return null;
        }

		return reducedGraph;
	}

	public void cancel() {
		this.cancel = true;
	}
}
