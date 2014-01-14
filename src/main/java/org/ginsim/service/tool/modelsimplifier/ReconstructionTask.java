package org.ginsim.service.tool.modelsimplifier;

import org.colomoto.common.task.AbstractTask;
import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.logicalmodel.tool.reduction.ModelReducer;
import org.ginsim.common.application.LogManager;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.initialstate.GsInitialStateList;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialState;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateList;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateManager;
import org.ginsim.core.graph.regulatorygraph.perturbation.ListOfPerturbations;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationManager;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.service.tool.reg2dyn.SimulationParameterList;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;
import org.ginsim.service.tool.reg2dyn.SimulationParametersManager;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClassDefinition;
import org.ginsim.service.tool.reg2dyn.priorityclass.PriorityClassManager;
import org.ginsim.service.tool.reg2dyn.priorityclass.Reg2dynPriorityClass;

import java.text.DateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Reconstruct a Regulatory Graph from a Logical model and restore layout,
 * comments and metadata from an original graph.
 * This is used by the reduction tool to show the reduced graph.
 *
 * @author Aurelien Naldi
 */
public class ReconstructionTask extends AbstractTask<RegulatoryGraph> {

	private final RegulatoryGraph graph;
    private final LogicalModel newModel;
    private final Collection<NodeInfo> to_remove;

	String s_comment = "";

    public ReconstructionTask(LogicalModel reducedModel, RegulatoryGraph graph) {
        this(reducedModel, graph, null);
    }

	public ReconstructionTask(LogicalModel reducedModel, RegulatoryGraph graph, ModelSimplifierConfig config) {
        this.graph = graph;
        this.newModel = reducedModel;
        if (config == null) {
            this.to_remove = new ArrayList<NodeInfo>();
        } else {
            this.to_remove = config.m_removed;
        }
	}
	
    public RegulatoryGraph doGetResult() {
        List<RegulatoryNode> oldNodeOrder = graph.getNodeOrder();

        // create the new regulatory graph
        RegulatoryGraph simplifiedGraph = LogicalModel2RegulatoryGraph.importModel(newModel, to_remove);
        Map<Object, Object> copyMap = new HashMap<Object, Object>();

		Annotation note = simplifiedGraph.getAnnotation();
		note.copyFrom(graph.getAnnotation());
		if (s_comment.length() > 2) {
			note.setComment("Model Generated by GINsim on "+
					DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()) +
					", by removing the following nodes: "+s_comment.substring(2)+
					"\n\n"+note.getComment());
		}

		//GsGraphManager<RegulatoryNode, RegulatoryMultiEdge> simplifiedManager = simplifiedGraph.getGraphManager();
		List<RegulatoryNode> simplified_nodeOrder = simplifiedGraph.getNodeOrder();

		// Create all the nodes of the new model
		NodeAttributesReader vreader = graph.getNodeAttributeReader();
		NodeAttributesReader simplified_vreader = simplifiedGraph.getNodeAttributeReader();
		for (RegulatoryNode vertex: graph.getNodeOrder()) {
            RegulatoryNode clone = simplifiedGraph.getNodeByName(vertex.getId());
			if (clone != null) {
				vreader.setNode(vertex);
				simplified_vreader.setNode(clone);
				simplified_vreader.copyFrom(vreader);
				copyMap.put(vertex, clone);
			}
		}

		// copy all unaffected edges
		EdgeAttributesReader ereader = graph.getEdgeAttributeReader();
		EdgeAttributesReader simplified_ereader = simplifiedGraph.getEdgeAttributeReader();
		for (RegulatoryMultiEdge me: graph.getEdges()) {
			RegulatoryNode src = (RegulatoryNode)copyMap.get(me.getSource());
			RegulatoryNode target = (RegulatoryNode)copyMap.get(me.getTarget());
			if (src != null && target != null) {
				RegulatoryMultiEdge me_clone = simplifiedGraph.getEdge(src, target);
                if (me_clone != null) {
					copyMap.put(me, me_clone);
					ereader.setEdge(me);
					simplified_ereader.setEdge(me_clone);
					simplified_ereader.copyFrom(ereader);
				}
			}
		}

		// build a mapping between new nodes and old position
		Map<RegulatoryNode, Integer> m_orderPos = new HashMap<RegulatoryNode, Integer>();
		Iterator<RegulatoryNode> it_oldOrder = oldNodeOrder.iterator();
		int pos = -1;
		for (RegulatoryNode vertex: simplified_nodeOrder) {;
			String id = vertex.getId();
			while (it_oldOrder.hasNext()) {
				pos++;
				RegulatoryNode oldNode = it_oldOrder.next();
				if (id.equals(oldNode.getId())) {
					m_orderPos.put(vertex, new Integer(pos));
					break;
				}
			}
		}

		// get as much of the associated data as possible
		Map m_alldata = new HashMap();
		
		// adapt perturbations which do not affect the removed components
		ListOfPerturbations perturbations = (ListOfPerturbations) ObjectAssociationManager.getInstance().getObject( graph, PerturbationManager.KEY, false);
		if (perturbations != null && perturbations.size() > 0) {
			ListOfPerturbations newPerturbations = (ListOfPerturbations) ObjectAssociationManager.getInstance().getObject( simplifiedGraph, PerturbationManager.KEY, true);
			Map<NodeInfo, NodeInfo> m_nodeinfos = new HashMap<NodeInfo, NodeInfo>();
			Map<Perturbation,Perturbation> m_perturbations = new HashMap<Perturbation, Perturbation>();
			for (RegulatoryNode vertex: oldNodeOrder) {
				RegulatoryNode clone = (RegulatoryNode)copyMap.get(vertex);
				if (clone != null) {
					m_nodeinfos.put(vertex.getNodeInfo(), clone.getNodeInfo());
				}
			}
			for (Perturbation p: perturbations) {
				Perturbation pnew = p.clone(newPerturbations, m_nodeinfos, m_perturbations);
				if (pnew != null) {
					m_perturbations.put(p, pnew);
				}
			}
		}

		
		// initial states
        GsInitialStateList linit = (GsInitialStateList) ObjectAssociationManager.getInstance().getObject( graph, InitialStateManager.KEY, false);
		if (linit != null && !linit.isEmpty()) {
			GsInitialStateList newLinit = (GsInitialStateList) ObjectAssociationManager.getInstance().getObject( simplifiedGraph, InitialStateManager.KEY, true);
            InitialStateList[] inits = {linit.getInitialStates(), linit.getInputConfigs()};
            InitialStateList[] newInits = {newLinit.getInitialStates(), newLinit.getInputConfigs()};

			for (int i=0 ; i<inits.length ; i++) {
                InitialStateList init = inits[i];
                InitialStateList newInit = newInits[i];
    			if (init != null && init.getNbElements(null) > 0) {
    				for (int j=0 ; j<init.getNbElements(null) ; j++) {
    					InitialState istate = (InitialState)init.getElement(null, j);
    					int epos = newInit.add();
    					InitialState newIstate = (InitialState)newInit.getElement(null, epos);
    					newIstate.setName(istate.getName());
    					m_alldata.put(istate, newIstate);
    					Map<NodeInfo, List<Integer>> m_init = newIstate.getMap();
    					for (Entry<NodeInfo, List<Integer>> e: istate.getMap().entrySet()) {
    						RegulatoryNode o = (RegulatoryNode)copyMap.get(e.getKey());
    						if (o != null) {
    							m_init.put( o.getNodeInfo(), e.getValue());
    						}
    					}
    				}
    			}
			}
		}
		
		// priority classes definition and simulation parameters
		SimulationParameterList params = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( graph, SimulationParametersManager.KEY, false);
		if (params != null) {
			PriorityClassManager pcman = params.pcmanager;
			SimulationParameterList new_params = (SimulationParameterList) ObjectAssociationManager.getInstance().getObject( simplifiedGraph, SimulationParametersManager.KEY, true);
			PriorityClassManager new_pcman = new_params.pcmanager;
			for (int i=2 ; i<pcman.getNbElements(null) ; i++) {
				PriorityClassDefinition pcdef = (PriorityClassDefinition)pcman.getElement(null, i);
				int index = new_pcman.add();
				PriorityClassDefinition new_pcdef = (PriorityClassDefinition)new_pcman.getElement(null, index);
				new_pcdef.setName(pcdef.getName());
				m_alldata.put(pcdef, new_pcdef);
				Map<Reg2dynPriorityClass, Reg2dynPriorityClass> m_pclass = new HashMap<Reg2dynPriorityClass, Reg2dynPriorityClass>();
				// copy all priority classes
				for (int j=0 ; j<pcdef.getNbElements(null) ; j++) {
					Reg2dynPriorityClass pc = (Reg2dynPriorityClass)pcdef.getElement(null, j);
					if (j>0) {
						new_pcdef.add();
					}
					Reg2dynPriorityClass new_pc = (Reg2dynPriorityClass)new_pcdef.getElement(null, j);
					new_pc.setName(pc.getName());
					new_pc.rank = pc.rank;
					new_pc.setMode(pc.getMode());
					m_pclass.put(pc, new_pc);
				}
				
				// properly place nodes
				for (Entry<?,?> e: pcdef.m_elt.entrySet()) {
					RegulatoryNode vertex = (RegulatoryNode)copyMap.get(e.getKey());
					if (vertex != null) {
						new_pcdef.m_elt.put(vertex,	m_pclass.get(e.getValue()));
					}
				}
			}
			int[] t_index = {0};
			new_pcman.remove(null, t_index);
			
			// simulation parameters
			for (int i=0 ; i<params.getNbElements() ; i++) {
			    SimulationParameters param = (SimulationParameters)params.getElement(null, i);
			    int index = new_params.add();
			    SimulationParameters new_param = (SimulationParameters)new_params.getElement(null, index);
			    m_alldata.put("", new_pcman);
			    param.copy_to(new_param, m_alldata);
			}
		}
		return simplifiedGraph;
	}
	
}
