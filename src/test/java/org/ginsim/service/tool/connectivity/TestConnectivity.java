package org.ginsim.service.tool.connectivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.Vector;

import org.ginsim.common.application.OptionStore;
import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.backend.EdgeAttributeReaderImpl;
import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.backend.NodeAttributeReaderImpl;
import org.ginsim.core.graph.reducedgraph.NodeReducedData;
import org.ginsim.core.graph.regulatorygraph.BasicRegulatoryGraphTest;
import org.ginsim.core.graph.regulatorygraph.RegulatoryEdgeSign;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.NodeBorder;
import org.ginsim.core.graph.view.NodeShape;
import org.ginsim.core.service.ServiceManager;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestConnectivity {
	
	@BeforeClass
	public static void beforeAllTests(){
		
		try {
			OptionStore.init( BasicRegulatoryGraphTest.class.getPackage().getName());
	    	OptionStore.getOption( EdgeAttributeReaderImpl.EDGE_COLOR, new Integer(-13395457));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_BG, new Integer(-26368));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_FG, new Integer(Color.WHITE.getRGB()));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_HEIGHT, new Integer(30));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_WIDTH, new Integer(55));
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_SHAPE, NodeShape.RECTANGLE.name());
	    	OptionStore.getOption( NodeAttributeReaderImpl.VERTEX_BORDER, NodeBorder.SIMPLE.name());
		} catch (Exception e) {
			fail( "Initialisation of OptionStore failed : " + e);
		}
	}
	
	/**
	 * Try to remove all the registered graphs from the GraphManager after each test
	 * 
	 */
	@After
	public void afterEachTest(){
		
		Vector<Graph> graph_list = new Vector( GraphManager.getInstance().getAllGraphs());
		
		if( graph_list != null && !graph_list.isEmpty()){
			
			for( Graph graph : graph_list){
				GraphManager.getInstance().close( graph);
			}
		}
	}
	
	@Test
	public void SimpleConnectivityTest() {
		RegulatoryGraph regGraph = GraphManager.getInstance().getNewGraph();
		assertNotNull( "Create graph : the graph is null.", regGraph);

		RegulatoryNode nodes[] = new RegulatoryNode[14];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = regGraph.addNode();
		}
		int edges[][] = {{0,0}, //trivial SCC
					{1, 2}, {2, 1}, //isolated cycle
					{3, 4}, {4, 3}, //cycle in a garden of eden
					{4, 5}, //path
					{5, 6}, {6, 7}, {7, 5}, //terminal cycle with 3 nodes
					{8, 9}, {9, 8}, {9, 10}, {10, 8}, //isolated cycle with 3 node and a sub-cycle
					{11, 12}, {11, 13}, {12, 13} //a tree, making it a cycle only in non oriented graphs
		};
		
		for (int[] edge : edges) {
			regGraph.addEdge(nodes[edge[0]], nodes[edge[1]], RegulatoryEdgeSign.POSITIVE);
		}

		ConnectivityService service = ServiceManager.getManager().getService(ConnectivityService.class);
        ConnectivityResult result = service.run(regGraph);
        assertNotNull("The service didn't return any result", result);
        
        //Count of SCC
        assertEquals("Wrong number of SCC", 8, result.getComponents().size());

        
        //Count of Trivial SCC
        int total = 0;
        for (NodeReducedData scc : result.getComponents()) {
			if (scc.isTrivial()) total++;
		}
        assertEquals("Wrong number of trivial SCC", 4, total);
        
        //Count of Transient SCC
        total = 0;
        for (NodeReducedData scc : result.getComponents()) {
			if (scc.isTransient(regGraph)) total++;
		}
        assertEquals("Wrong number of transient SCC", 3, total);
        
        //Count of nodes in the SCC
        total = 0;
        for (NodeReducedData scc : result.getComponents()) {
			total += scc.getContent().size();
		}
        assertEquals("Wrong number of nodes in the SCC", 14, total);
        
        
		GraphManager.getInstance().close( regGraph);

	}
}
