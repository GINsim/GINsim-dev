package org.ginsim.servicegui.tool.stablestates;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.ginsim.common.application.Translator;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.gui.service.AbstractServiceGUI;
import org.ginsim.gui.service.ServiceGUI;
import org.ginsim.gui.service.common.GUIFor;
import org.ginsim.gui.service.common.ServiceStatus;
import org.ginsim.gui.service.common.ToolAction;
import org.ginsim.gui.utils.dialog.stackdialog.HandledStackDialog;
import org.ginsim.service.tool.stablestates.StableStatesService;
import org.mangosdk.spi.ProviderFor;


/**
 * Define the action for stableStates service
 */
@ProviderFor( ServiceGUI.class)
@GUIFor( StableStatesService.class)
@ServiceStatus( ServiceStatus.RELEASED)
public class StableStatesServiceGUI extends AbstractServiceGUI {
	    
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		
		if (graph instanceof RegulatoryGraph) {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new StableStatesAction( (RegulatoryGraph)graph, this));
			return actions;
		}
		return null;
	}

	@Override
	public int getInitialWeight() {
		return W_TOOLS_MAIN + 20;
	}
}

class StableStatesAction extends ToolAction {

	private static final long serialVersionUID = -6910432442433953521L;
	private final RegulatoryGraph graph;
	
	public StableStatesAction(RegulatoryGraph graph, ServiceGUI serviceGUI) {
		
		super( "STR_stableStates", "STR_stableStates_descr", serviceGUI);
		this.graph = graph;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
    	if (graph.getNodeOrderSize() < 1) {
            NotificationManager.publishWarning( graph, Translator.getString("STR_emptyGraph"));
    		return;
    	}

    	new HandledStackDialog( new StableStateUI( graph));
	}
	
}
