package fr.univmrs.ibdm.GINsim.reg2dyn;

import javax.swing.JFrame;

import fr.univmrs.ibdm.GINsim.global.GsException;
import fr.univmrs.ibdm.GINsim.graph.GsActionProvider;
import fr.univmrs.ibdm.GINsim.graph.GsGraph;
import fr.univmrs.ibdm.GINsim.graph.GsGraphNotificationMessage;
import fr.univmrs.ibdm.GINsim.gui.GsActions;
import fr.univmrs.ibdm.GINsim.gui.GsPluggableActionDescriptor;
import fr.univmrs.ibdm.GINsim.manageressources.Translator;
import fr.univmrs.ibdm.GINsim.plugin.GsPlugin;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsMutantListManager;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryGraphDescriptor;

/**
 * main method for the reg2dyn plugin
 * 
 * @author aurelien
 */
public class Reg2DynPlugin implements GsPlugin, GsActionProvider {

    static {
        if (!GsRegulatoryGraphDescriptor.isObjectManagerRegistred("mutant")) {
            GsRegulatoryGraphDescriptor.registerObjectManager(new GsMutantListManager());
        }
        GsRegulatoryGraphDescriptor.registerObjectManager(new GsSimulationParametersManager());
    }
    
    private GsPluggableActionDescriptor[] t_action = null;

    public void registerPlugin() {
        GsRegulatoryGraphDescriptor.registerActionProvider(this);
    }

    public GsPluggableActionDescriptor[] getT_action(int actionType,
            GsGraph graph) {
        if (actionType != ACTION_ACTION) {
            return null;
        }
        if (t_action == null) {
            t_action = new GsPluggableActionDescriptor[1];
            t_action[0] = new GsPluggableActionDescriptor("STR_reg2dyn",
                    "STR_reg2dyn_descr", null, this, ACTION_ACTION, 0);
        }
        return t_action;
    }

    public void runAction(int actionType, int ref, GsGraph graph, JFrame frame) throws GsException {
        if (actionType != ACTION_ACTION) {
            return;
        }
        if (graph.getNodeOrder().size() < 1) {
            graph.addNotificationMessage(new GsGraphNotificationMessage(graph, Translator.getString("STR_emptyGraph"), GsGraphNotificationMessage.NOTIFICATION_WARNING));
            return;
        }
		if (ref == 0) {
//            Map m_params = (Map)graph.getObject("reg2dyn_parameters");
//            if (m_params == null) {
//                m_params = new HashMap();
//                graph.addObject("reg2dyn_parameters", m_params);
//            }
//            new Reg2dynFrame(frame, (GsRegulatoryGraph)graph, m_params).setVisible(true);
            graph.getGraphManager().getMainFrame().getGsAction().setCurrentMode(GsActions.MODE_DEFAULT, 0, false);

            GsSimulationParameterList paramList = (GsSimulationParameterList)graph.getObject("reg2dyn_parameters");
            if (paramList == null) {
                paramList = new GsSimulationParameterList(graph);
                graph.addObject("reg2dyn_parameters", paramList);
            }
            new GsReg2dynFrame(frame, paramList).setVisible(true);
		}
	}
}
