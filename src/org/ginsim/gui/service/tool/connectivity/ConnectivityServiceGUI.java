package org.ginsim.gui.service.tool.connectivity;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.exception.GsException;
import org.ginsim.graph.common.Graph;
import org.ginsim.graph.reducedgraph.NodeReducedData;
import org.ginsim.graph.reducedgraph.ReducedGraph;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.common.GUIFor;
import org.ginsim.gui.service.common.GenericGraphAction;
import org.ginsim.gui.service.common.ToolAction;
import org.ginsim.gui.shell.FileSelectionHelper;
import org.ginsim.service.tool.connectivity.ConnectivityService;
import org.ginsim.utils.log.LogManager;
import org.mangosdk.spi.ProviderFor;


/**
 * register the connectivity service
 */
@ProviderFor(ServiceGUI.class)
@GUIFor(ConnectivityService.class)
public class ConnectivityServiceGUI implements ServiceGUI {

	@Override
	public List<Action> getAvailableActions( Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		if (graph instanceof ReducedGraph) {
			actions.add( new ConnectivityExtractAction( (ReducedGraph)graph));
		} else {
			actions.add( new ConnectivityAction( graph));
		}
		
		return actions;
	}
}

class ConnectivityExtractAction extends ToolAction {
	
	private final ReducedGraph graph;
	
	protected ConnectivityExtractAction( ReducedGraph graph) {
        super( "STR_connectivityExtract", null, "STR_connectivityExtract_descr", null);
		this.graph = graph;
	}
	
	@Override
	public void actionPerformed( ActionEvent arg0) {
		
        String s_ag = null;
		try {
			s_ag = graph.getAssociatedGraphID();
		} catch (GsException e) {
			LogManager.debug();
			return;
		}
		
        if (s_ag != null) {
        	List<NodeReducedData> selected = GUIManager.getInstance().getGraphGUI(graph).getSelection().getSelectedNodes();
        	
	        Graph subgraph = FileSelectionHelper.open( s_ag, graph.getSelectedMap(selected));
	        if (subgraph != null) {
	            GUIManager.getInstance().whatToDoWithGraph( subgraph, graph, true);
	        }
        }
	}
}


class ConnectivityAction extends GenericGraphAction {
	
	protected ConnectivityAction( Graph graph) {
        super( graph, "STR_connectivity", null, "STR_connectivity_descr", null);
	}
	
	@Override
	public void actionPerformed( ActionEvent arg0) {
        new ConnectivityFrame( GUIManager.getInstance().getFrame(graph), graph);
	}
}
