package fr.univmrs.ibdm.GINsim.regulatoryGraph;

import java.io.IOException;
import java.util.*;

import fr.univmrs.ibdm.GINsim.annotation.Annotation;
import fr.univmrs.ibdm.GINsim.data.GsDirectedEdge;
import fr.univmrs.ibdm.GINsim.data.ToolTipsable;
import fr.univmrs.ibdm.GINsim.global.Tools;
import fr.univmrs.ibdm.GINsim.graph.GsGraph;
import fr.univmrs.ibdm.GINsim.graph.GsGraphNotificationAction;
import fr.univmrs.ibdm.GINsim.graph.GsGraphNotificationMessage;
import fr.univmrs.ibdm.GINsim.manageressources.Translator;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.GsTreeInteractionsModel;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeExpression;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeString;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeValue;
import fr.univmrs.ibdm.GINsim.xml.GsXMLWriter;
import fr.univmrs.ibdm.GINsim.xml.GsXMLize;

/**
 * the Class in which we store biological data for vertices (genes).
 *
 * a gene has:
 * <ul>
 *		<li> a baseValue, to which it tends when no interactions is active</li>
 *		<li> a maxValue</li>
 *		<li> notes (comment and URLs)</li>
 *		<li> a list of interactions: each of them describes states to
 *			which the gene tends when some incoming edges are actives </li>
 * </ul>
 */
public class GsRegulatoryVertex implements ToolTipsable, GsXMLize {

	private short 			maxValue;
	private final LogicalParameterList v_logicalParameters;

	private String 			name;
	private Annotation	gsa;
	private String 			id;

	private GsTreeInteractionsModel interactionsModel;
	private GsRegulatoryGraph graph;

    private static final String S_ID   = Translator.getString("STR_id")+" : ";
    private static final String S_NAME = " | "+ Translator.getString("STR_name")+" : ";
    private static final String S_MAX  = " | "+ Translator.getString("STR_max") +" : ";

    public static final int MAXVALUE = 9;

	/**
	 * Constructs an empty vector and set the baseValue (0) and the maxValue (1)
	 * @param id
	 */
	public GsRegulatoryVertex(String id, GsRegulatoryGraph graph) {
		super();
		name			= "";
		maxValue 		= 1;
		gsa				= new Annotation();
		v_logicalParameters 	= new LogicalParameterList();
		this.id = id;
		this.graph = graph;
		interactionsModel = new GsTreeInteractionsModel(graph);
	}

	/**
	 * @param num number of the gene.
	 */
	public GsRegulatoryVertex(int num, GsRegulatoryGraph graph) {
		super();
		name			= "";
		maxValue 		= 1;
		gsa				= new Annotation();
		v_logicalParameters 	= new LogicalParameterList();
		this.id = "G"+num;
		this.graph = graph;
		interactionsModel = new GsTreeInteractionsModel(graph);
	}

	/**
	 * @return the max value of the node
	 */
	public short getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets the max value to the node
	 * @param max the new max value
	 * @param graph the graph (to propagate changes if needed)
	 */
	public void setMaxValue(short max, GsRegulatoryGraph graph) {
	    if (max>0 && max<= MAXVALUE && max != maxValue) {
	    	short oldmax = maxValue;
	    	if (oldmax > max) {
	    		List l_fixable = new ArrayList();
	    		List l_conflict = new ArrayList();
	    		List l_parameters = new ArrayList();
			    if (!getInteractionsModel().isMaxCompatible(max)) {
			    	graph.addNotificationMessage( new GsGraphNotificationMessage(graph, "Max value (" + max + ") is inconsistent with some boolean function value.", GsGraphNotificationMessage.NOTIFICATION_ERROR) );
			    	return;
			    }
	    		graph.canApplyNewMaxValue(this, max, l_fixable, l_conflict);
	    		v_logicalParameters.applyNewMaxValue(max, graph, l_parameters);
	    		if (l_fixable.size() > 0 || l_conflict.size() > 0 || l_parameters.size() > 0) {
	    			GsGraphNotificationAction action = new UpdateMaxBlockedAction(this, max, l_fixable, l_conflict, l_parameters);
		    		graph.addNotificationMessage(new GsGraphNotificationMessage(
		    				graph, "max value decrease is blocked", action, null, GsGraphNotificationMessage.NOTIFICATION_WARNING) );
	    			return;
	    		}
	    	}
	    	maxValue = max;
    		graph.fireGraphChange(GsGraph.CHANGE_VERTEXUPDATED, this);
    		getInteractionsModel().refreshVertex();
	    }
	}

	/**
	 * @return the id of this vertex.
	 */
	public String getId() {
	    return id;
	}

	/**
	 * change the id of this vertex.
	 * @param id the new id.
	 */
	public boolean setId(String id) {
		if (Tools.isValidId(id)) {
			this.id = id;
			return true;
		}
		return false;
	}
	/**
	 * Adds the specified interaction I to the interactions of the node
	 * @param I
	 * @return true if the logical parameter has been added
	 */
	public boolean addLogicalParameter (GsLogicalParameter I, boolean manual) {
		return v_logicalParameters.addLogicalParameter(I, manual);
	}

	/**
	 * Removes the specified interaction to the interactions of the node
	 * @param I
	 */
	public void removeInteraction (GsLogicalParameter I) {
		v_logicalParameters.remove(I);
	}

	/**
	 * Returns the interaction at the specified position index
	 * @param index
	 * @return GsLogicalParameter
	 */
	public GsLogicalParameter getInteraction(int index) {
		try
		{
			return (GsLogicalParameter)v_logicalParameters.get(index);
		}
		catch (java.lang.ArrayIndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * change an interaction's list of edges: we have to check that the new list of edges
	 * is not in an existing interaction.
	 *
	 * @param index
	 * @param edges
	 */
	public void updateInteraction(int index, Vector edges) {
		v_logicalParameters.updateInteraction(index, edges);
	}

	/**
	 * Returns the number of the interaction in the node.
	 * @return the number of user defined interactions on this node
	 */
	public int interactionCount() {
		return v_logicalParameters.getRealSize();
	}

	/**
	 * get the long name of the node.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * set the name of the gene.
	 * @param name the new name
	 */
	public void setName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
		}
	}

	/**
	 * get notes on this gene.
	 * @return the annotation associated to this node
	 */
	public Annotation getAnnotation() {
		return gsa;
	}

	/**
	 * @return the list of all interactions on this gene.
	 */
	public LogicalParameterList getV_logicalParameters() {
		return v_logicalParameters;
	}

    /**
     * get the DAG representation of logical parameters.
     *
     * @param graph
     * @return an OmddNode representing logical parameters associated to this vertex.
     */
    public OmddNode getTreeParameters(GsRegulatoryGraph graph) {
        OmddNode root;
        root = OmddNode.TERMINALS[0];
        OmddNode curNode;
        Iterator it = v_logicalParameters.iterator();
        while (it.hasNext()) {
            GsLogicalParameter gsi = (GsLogicalParameter)it.next();
            curNode = gsi.buildTree(graph, this);
            if (curNode != null) {
                root = root.merge(curNode, OmddNode.OR);
            }
        }
        return root;
    }

	public String toToolTip() {
		return    S_ID  + id
				+ S_NAME+ name
                + S_MAX + maxValue;
	}

	public void toXML(GsXMLWriter out, Object param, int mode) throws IOException {

			out.openTag("node");
	    	out.addAttr("id", getId());
			if (name.length()>0) {
			    out.addAttr("name", name);
			}
		    out.addAttr("maxvalue", ""+maxValue);

			// TODO: at some point stop saving logical parameters
		    Iterator it = v_logicalParameters.iterator();
		    while (it.hasNext()) {
				((GsLogicalParameter) it.next()).toXML(out, null, mode);
			}
			// save logical functions
			saveInteractionsModel(out, mode);

			gsa.toXML(out, null, mode);

			if (param != null) {
			    out.addContent("\n");
			    out.write(param.toString());
			}
			out.closeTag();
	}

	/**
	 * @return annotation for this node
	 */
	public Annotation getGsa() {
		return gsa;
	}

	/**
	 * @param annotation for this node.
	 */
	public void setGsa(Annotation annotation) {
		gsa = annotation;
	}

	public Object clone() {
		GsRegulatoryVertex clone = new GsRegulatoryVertex(id, graph);
		clone.maxValue = maxValue;
		clone.name = name;
		clone.setGsa((Annotation)gsa.clone());
		return clone;
	}

	/**
	 * update logical parameters when an interaction is deleted.
	 */
	public void cleanupInteraction() {
		v_logicalParameters.cleanupInteraction();
	}


	/**
	 * when an incoming edge is deleted, we don't want to see it appear in interactions anymore.
	 * This will remove a subedge.
	 * NOTE: ALL interaction involving the deleted subedge will be suppressed.
	 * @param multiEdge
	 * @param index
	 */
	public void removeEdgeFromInteraction(GsRegulatoryMultiEdge multiEdge, int index) {
		cleanupInteraction();
		interactionsModel.removeEdge(multiEdge, index);
	}

	/**
	 * when an incoming edge is deleted, we don't want to see it appear in interactions anymore.
	 * This will remove all subedges of a multiedge.
	 * NOTE: ALL interaction involving the deleted multiedge will be suppressed.
	 * @param multiEdge
	 */
	public void removeEdgeFromInteraction(GsRegulatoryMultiEdge multiEdge) {
		cleanupInteraction();
		interactionsModel.removeEdge(multiEdge);
	}

    public String toString() {
        return id;
    }

    /**
     * @param copyMap
     */
    public void cleanupInteractionForNewGraph(Map copyMap) {
        GsRegulatoryVertex myClone = (GsRegulatoryVertex) copyMap.get(this);
        Iterator it = v_logicalParameters.iterator();
        while (it.hasNext()) {
            ((GsLogicalParameter)it.next()).applyNewGraph(myClone, copyMap);
            // TODO: copy the logical functions as well
            // if pasted into a new graph, the "interactionModel" should be
            // recreated/updated for the new graph
        }
    }

    public void setInteractionsModel(GsTreeInteractionsModel model) {
      interactionsModel = model;
      v_logicalParameters.setFunctionParameters(interactionsModel.getLogicalParameters());
    }

    public GsTreeInteractionsModel getInteractionsModel() {
      return interactionsModel;
    }

    public void saveInteractionsModel(GsXMLWriter out, int mode) throws IOException {
      GsTreeString root = (GsTreeString)interactionsModel.getRoot();
      GsTreeValue val;
      GsTreeElement exp;

      for (int i = 0; i < root.getChildCount(); i++) {
        val = (GsTreeValue)root.getChild(i);
        out.openTag("value");
        out.addAttr("val", ""+val.getValue());
        for (int j = 0; j < val.getChildCount(); j++) {
          exp = val.getChild(j);
          if (exp instanceof GsTreeExpression) {
            out.openTag("exp");
            out.addAttr("str", exp.toString());
            out.closeTag();
          }
        }
        out.closeTag();
      }
    }
    public void incomingEdgeAdded(GsRegulatoryMultiEdge me) {
      interactionsModel.addEdge(me);
    }

	public void addParametersForMDDLeaf(short value, List edges, Map m_edges) {
		// use a more efficient and convenient representation
		int[] t_curVal = new int[edges.size()];
		int[][] t_values = new int[t_curVal.length][2];
		GsRegulatoryMultiEdge[] t_me = new GsRegulatoryMultiEdge[t_curVal.length];
		
		// put the right value in the right place
		for (int i=0 ; i<t_curVal.length ; i++) {
			GsDirectedEdge de = (GsDirectedEdge)edges.get(i);
			GsRegulatoryMultiEdge me = (GsRegulatoryMultiEdge)de.getUserObject();
			t_me[i] = me;
			
			// transform values into edge indexes
			int[] vals = (int[])m_edges.get(de.getSourceVertex());
			int ei = 0;
			for ( ; ei<me.getEdgeCount() ; ei++) {
				if (vals[0] < me.getMin(ei)) {
					t_values[i][0] = ei-1;
					break;
				}
			}
			for ( ; ei<me.getEdgeCount() ; ei++) {
				if (vals[1] < me.getMin(ei)) {
					t_values[i][1] = ei-1;
					break;
				}
			}
			if (ei > me.getEdgeCount()) {
				t_values[i][1] = ei-1;
			}
			t_curVal[i] = t_values[i][0];
		}
		
		// now do the real job
		while (true) {
			Vector v = new Vector();
			int lastIndex = -1;
			for (int i=0 ; i<t_curVal.length ; i++) {
				if (t_curVal[i] != -1) {
					// add interaction to the vector
					v.add(t_me[i].getEdge(t_curVal[i]));
				}
				if (t_curVal[i] < t_values[i][1]) {
					lastIndex = i;
				}
			}
			
			addLogicalParameter(new GsLogicalParameter(v, value), true);

			// stop if no free value was found
			if (lastIndex == -1) {
				break;
			}
			// go to next step
			t_curVal[lastIndex]++;
			for (int i=lastIndex+1 ; i<t_curVal.length ; i++) {
				t_curVal[i] = t_values[i][0];
			}
		}
	}
}

class UpdateMaxBlockedAction implements GsGraphNotificationAction {

	String[] t_action;
	
	GsRegulatoryVertex vertex;
	short max;
	List l_fixable, l_conflict, l_parameters;
	
	public UpdateMaxBlockedAction(GsRegulatoryVertex vertex,
			short max, List l_fixable, List l_conflict, List l_parameters) {
		this.vertex = vertex;
		this.max = max;
		this.l_conflict = l_conflict;
		this.l_fixable = l_fixable;
		this.l_parameters = l_parameters;
		if (l_conflict.size() == 0) {
			t_action = new String[2];
			t_action[1] = "Fix";
		} else {
			t_action = new String[1];
		}
		t_action[0] = "Detail";
	}

	public String[] getActionName() {
		return t_action;
	}

	public boolean perform(GsGraph graph, Object data, int index) {
		if (index == 1) {
			if (l_conflict.size() > 0) {
				return false;
			}
			Iterator it = l_fixable.iterator();
			while (it.hasNext()) {
				((GsRegulatoryMultiEdge)it.next()).applyNewMaxValue(max);
			}
			it = l_parameters.iterator();
			while (it.hasNext()) {
				((GsLogicalParameter)it.next()).setValue(max, graph);
			}
			vertex.setMaxValue(max, (GsRegulatoryGraph)graph);
			return true;
		}
		
		// TODO Auto-generated method stub
		return true;
	}

	public boolean timeout(GsGraph graph, Object data) {
		return true;
	}
	
}