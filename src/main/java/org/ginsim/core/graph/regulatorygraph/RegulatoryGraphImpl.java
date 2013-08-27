package org.ginsim.core.graph.regulatorygraph;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.LogicalModelImpl;
import org.colomoto.logicalmodel.NodeInfo;
import org.colomoto.mddlib.MDDManager;
import org.colomoto.mddlib.MDDManagerFactory;
import org.colomoto.mddlib.MDDVariableFactory;
import org.ginsim.common.application.GsException;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.annotation.AnnotationLink;
import org.ginsim.core.annotation.BiblioManager;
import org.ginsim.core.graph.GraphManager;
import org.ginsim.core.graph.common.AbstractGraph;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.common.GraphChangeType;
import org.ginsim.core.graph.dynamicgraph.DynamicGraph;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.core.graph.regulatorygraph.perturbation.PerturbationManager;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.EdgeStyle;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.css.CSSFilesAssociatedManager;
import org.ginsim.core.io.parser.GinmlHelper;
import org.ginsim.core.notification.NotificationManager;
import org.ginsim.core.notification.resolvable.NotificationResolution;

/**
 * Implementation of the RegulatoryGraph interface.
 * 
 * @author Aurelien Naldi
 */
public final class RegulatoryGraphImpl  extends AbstractGraph<RegulatoryNode, RegulatoryMultiEdge> 
	implements RegulatoryGraph{

	public static final String GRAPH_ZIP_NAME = "regulatoryGraph.ginml";
	
	private int nextid=0;

	private List<RegulatoryNode> nodeOrder = new ArrayList<RegulatoryNode>();

    static {
    	ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new PerturbationManager());
    	ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class,  new BiblioManager());
    	ObjectAssociationManager.getInstance().registerObjectManager( RegulatoryGraph.class, new CSSFilesAssociatedManager());
    }

    /**
     * Create a new Regulatory graph (not for parsing)
     */
    public RegulatoryGraphImpl() {
        this( false);
    }

    /**
     * @param parsing
     */
    public RegulatoryGraphImpl( boolean parsing) {
        super( RegulatoryGraphFactory.getInstance().getGraphType(), parsing);
    }
    
    @Override
    public List<RegulatoryNode> getNodeOrder() {
    	return nodeOrder;
    }
    
	@Override
	public List<NodeInfo> getNodeInfos() {
		return getNodeInfos(nodeOrder);
	}

    @Override
	public int getNodeOrderSize(){
		
		if( nodeOrder != null){
			return nodeOrder.size();
		}
		else{
			return 0;
		}
	}
	
    @Override
    public void setNodeOrder( List<RegulatoryNode> nodeOrder) {
		this.nodeOrder = nodeOrder;
	}
    
    @Override
    public RegulatoryNode addNode() {

        while ( getNodeByName("G" + nextid) != null) {
        		nextid++;
        }
        RegulatoryNode obj = new RegulatoryNode(nextid++, this);
        if (addNode(obj)) {
    		return obj;
        }
        return null;
    }
    
    @Override
	public boolean addNode( RegulatoryNode node){
		
        if (node != null && super.addNode( node)) {
    		nodeOrder.add( node);
    		return true;
        }
        
        return false;
	}
    
    @Override
    public RegulatoryMultiEdge addEdge(RegulatoryNode source, RegulatoryNode target, RegulatoryEdgeSign sign) {
    	RegulatoryMultiEdge obj = getEdge(source, target);
    	if (obj != null) {

    		// TODO: restore this action without requiring to know the GUIManager?
    		NotificationResolution resolution = null;
//    		NotificationResolution resolution = new NotificationResolution(){
//    			
//    			public boolean perform( Graph graph, Object[] data, int index) {
//    				
//    				GUIManager.getInstance().getGraphGUI(graph).selectEdge((Edge<?>)data[0]);
//    				return true;
//    			}
//    			
//    			public String[] getOptionsName(){
//    				
//    				String[] t_option = {"Go"};
//    				return t_option;
//    			}
//    		};
    		
    		NotificationManager.publishResolvableWarning( this, "STR_usePanelToAddMoreEdges", this, new Object[] {obj}, resolution);
    		
    		return obj;
    	}
    	obj = new RegulatoryMultiEdge(this, source, target, sign);
    	addEdge(obj);
    	obj.rescanSign( this);
    	target.incomingEdgeAdded(obj);
    	return obj;
    }

    
	@Override
	protected EdgeStyle<RegulatoryNode, RegulatoryMultiEdge> createDefaultEdgeStyle() {
		return new DefaultRegulatoryEdgeStyle();
	}

    @Override
	protected String getGraphZipName(){
		return GRAPH_ZIP_NAME;
	}
    
	@Override
    protected void doSave( OutputStreamWriter os, Collection<RegulatoryNode> vertices, Collection<RegulatoryMultiEdge> edges) throws GsException {
    	try {
            XMLWriter out = new XMLWriter(os, GinmlHelper.DEFAULT_URL_DTD_FILE);
	  		out.write("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
			out.write("\t<graph id=\"" + getGraphName() + "\"");
			out.write(" class=\"regulatory\"");
			out.write(" nodeorder=\"" + stringNodeOrder() +"\"");
			out.write(">\n");
			saveNodes(out, vertices);
			saveEdges(out, edges);
            if (graphAnnotation != null) {
            	graphAnnotation.toXML(out);
            }
	  		out.write("\t</graph>\n");
	  		out.write("</gxl>\n");
        } catch (IOException e) {
            throw new GsException( "STR_unableToSave", e);
        }
    }

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveEdges(XMLWriter out, Collection<RegulatoryMultiEdge> edges) throws IOException {
    	
        Iterator<RegulatoryMultiEdge> it = edges.iterator();

	    EdgeAttributesReader ereader = getCachedEdgeAttributeReader();
	    NodeAttributesReader nreader = getCachedNodeAttributeReader();
        while (it.hasNext()) {
        	RegulatoryMultiEdge edge = it.next();
            ereader.setEdge(edge);
            edge.toXML(out, GinmlHelper.getEdgeVS(ereader, nreader, edge));
        }
    }

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveNodes(XMLWriter out, Collection<RegulatoryNode> vertices) throws IOException {
    	
    	Iterator<RegulatoryNode> it = vertices.iterator();
    	
		NodeAttributesReader<RegulatoryNode> vReader = getNodeAttributeReader(); 
    	
        for (RegulatoryNode vertex: vertices) {
            vReader.setNode(vertex);
            vertex.toXML(out, vReader);
        }
    }

    @Override
    public boolean idExists(String newId) {
        Iterator it = getNodes().iterator();
        while (it.hasNext()) {
            if (newId.equals(((RegulatoryNode)it.next()).getId())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void changeNodeId(Object node, String newId) throws GsException {
        RegulatoryNode rvertex = (RegulatoryNode)node;
        if (newId.equals(rvertex.getId())) {
            return;
        }
        if (idExists(newId)) {
        	throw  new GsException(GsException.GRAVITY_ERROR, "id already exists");
        }
        if (!rvertex.setId(newId)) {
        	throw  new GsException(GsException.GRAVITY_ERROR, "invalid id");
        }
        refresh(node);
        fireMetaChange();
    }

    @Override
    public boolean removeEdge(RegulatoryMultiEdge edge) {
       edge.markRemoved();
       super.removeEdge(edge);
       RegulatoryNode src = edge.getSource();
       Collection<RegulatoryMultiEdge> targets = getOutgoingEdges(src);
       if (targets == null || targets.size() == 0) {
    	   src.setOutput(true, this);
       }
       edge.getTarget().removeEdgeFromInteraction(edge);
       fireGraphChange(GraphChangeType.EDGEREMOVED, edge);
       return true;
    }

    @Override
    public boolean removeNode(RegulatoryNode obj) {
    	List<RegulatoryMultiEdge> outedges = new ArrayList<RegulatoryMultiEdge>();
        for (RegulatoryMultiEdge me: getOutgoingEdges(obj)) {
        	outedges.add(me);
        }
        for (RegulatoryMultiEdge me: outedges) {
            removeEdge(me);
        }
        super.removeNode( obj);
        nodeOrder.remove(obj);
        fireGraphChange(GraphChangeType.NODEREMOVED, obj);
        return true;
    }

    @Override
    public RegulatoryNode addNewNode(String id, String name, byte max) {
    	RegulatoryNode existing = getNodeByName(id);
    	if (existing != null) {
    		throw new RuntimeException("A node with id \""+id+"\" already exists");
    	}
        RegulatoryNode vertex = new RegulatoryNode(id, this);
        if (name != null) {
            vertex.setName(name);
        }
        vertex.setMaxValue(max, this);
        addNode(vertex);

        return vertex;
    }

    @Override
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, String sign)  throws GsException{
    	RegulatoryEdgeSign vsign = RegulatoryEdgeSign.UNKNOWN;
    	for (RegulatoryEdgeSign s : RegulatoryEdgeSign.values()) {
    		if (s.getLongDesc().equals(sign)) {
    			vsign = s;
    			break;
    		}
    	}
    	return addNewEdge(from, to, minvalue, vsign);
    }
    
    @Override
    public RegulatoryEdge addNewEdge(String from, String to, byte minvalue, RegulatoryEdgeSign sign) throws GsException {
        RegulatoryNode source = null;
        RegulatoryNode target = null;

        source = (RegulatoryNode) getNodeByName(from);
        if (from.equals(to)) {
            target = source;
        } else {
            target = (RegulatoryNode) getNodeByName(to);
        }

        if (source == null || target == null) {
            throw new GsException( GsException.GRAVITY_ERROR, "STR_noSuchNode");
        }
        RegulatoryMultiEdge me = getEdge(source, target);
        int index = 0;
        if (me == null) {
            me = new RegulatoryMultiEdge(this, source, target, sign, minvalue);
            addEdge(me);
        } else {
            index = me.addEdge(sign, minvalue, this);
        }
        return me.getEdge(index);
    }

    @Override
	public void canApplyNewMaxValue(RegulatoryNode node, byte newMax, List l_fixable, List l_conflict) {
		for (RegulatoryMultiEdge me: getOutgoingEdges(node)) {
			me.canApplyNewMaxValue(newMax, l_fixable, l_conflict);
		}
	}

	private String stringNodeOrder() {
		String s = "";
		for (int i=0 ; i<nodeOrder.size() ; i++) {
			s += nodeOrder.get(i)+" ";
		}
		if (s.length() > 0) {
			return s.substring(0, s.length()-1);
		}
		return s;
	}

    @Override
    protected List doMerge( Graph<RegulatoryNode, RegulatoryMultiEdge> otherGraph) {
        if (!(otherGraph instanceof RegulatoryGraph)) {
            return null;
        }
        List ret = new ArrayList();
        HashMap copyMap = new HashMap();
        Iterator<RegulatoryNode> it = otherGraph.getNodes().iterator();
        NodeAttributesReader vReader = getNodeAttributeReader();
        NodeAttributesReader cvreader = otherGraph.getNodeAttributeReader();
        while (it.hasNext()) {
            RegulatoryNode vertexOri = (RegulatoryNode)it.next();
            RegulatoryNode vertex = (RegulatoryNode)vertexOri.clone();
            addNodeWithNewId(vertex);
            cvreader.setNode(vertexOri);
            vReader.setNode(vertex);
            vReader.copyFrom(cvreader);
            vReader.refresh();
            copyMap.put(vertexOri, vertex);
            ret.add(vertex);
        }

        Iterator<RegulatoryMultiEdge> it2 = otherGraph.getEdges().iterator();
        EdgeAttributesReader eReader = getEdgeAttributeReader();
        EdgeAttributesReader cereader = otherGraph.getEdgeAttributeReader();
        while (it2.hasNext()) {
        	RegulatoryMultiEdge deOri = it2.next();
        	RegulatoryMultiEdge edge = addEdge((RegulatoryNode)copyMap.get(deOri.getSource()), (RegulatoryNode)copyMap.get(deOri.getTarget()), RegulatoryEdgeSign.POSITIVE);
            edge.copyFrom(deOri);
            cereader.setEdge(deOri);
            eReader.setEdge(edge);
            eReader.copyFrom(cereader);
            eReader.refresh();
            copyMap.put(deOri, edge);
            ret.add(edge);
        }

        it = otherGraph.getNodes().iterator();
        while (it.hasNext()) {
            it.next().cleanupInteractionForNewGraph(copyMap);
        }
        return ret;
    }
    
    
    /**
     * @param node
     */
    private void addNodeWithNewId(RegulatoryNode node) {
        String id = node.getId();
        if (getNodeByName(id) == null) {
            addNode(node);
            return;
        }
        int addon = 1;
        while ( getNodeByName(id+"_"+addon) != null) {
            addon++;
        }
        node.setId(id+"_"+addon);
        addNode(node);
    }

    @Override
    public Graph getSubgraph(Collection<RegulatoryNode> v_vertex, Collection<RegulatoryMultiEdge> v_edges) {
    	
        RegulatoryGraph copiedGraph = GraphManager.getInstance().getNewGraph();
        NodeAttributesReader vReader = getNodeAttributeReader();
        NodeAttributesReader cvreader = copiedGraph.getNodeAttributeReader();
        HashMap copyMap = new HashMap();
        if (v_vertex != null) {
            for (RegulatoryNode vertexOri: v_vertex) {
                RegulatoryNode vertex = (RegulatoryNode)vertexOri.clone();
                ((RegulatoryGraphImpl)copiedGraph).addNodeWithNewId(vertex);
                vReader.setNode(vertexOri);
                cvreader.setNode(vertex);
                cvreader.copyFrom(vReader);
                copyMap.put( vertexOri, vertex);
            }
        }

        if (v_edges != null) {
        	EdgeAttributesReader eReader = getEdgeAttributeReader();
            EdgeAttributesReader cereader = copiedGraph.getEdgeAttributeReader();
	        for (RegulatoryMultiEdge edgeOri: v_edges) {
	        	RegulatoryMultiEdge edge = copiedGraph.addEdge((RegulatoryNode)copyMap.get(edgeOri.getSource()), (RegulatoryNode)copyMap.get(edgeOri.getTarget()), RegulatoryEdgeSign.POSITIVE);
	            edge.copyFrom(edgeOri);
	            copyMap.put(edgeOri, edge);
                eReader.setEdge(edgeOri);
                cereader.setEdge(edge);
                cereader.copyFrom(eReader);
	        }
        }

        if (v_vertex != null) {
            for (RegulatoryNode v: v_vertex) {
                v.cleanupInteractionForNewGraph(copyMap);
            }
        }

        return copiedGraph;
    }

    /**
     * Test if an association between a regulatory graph and a state transition graph is valid:
     * all what we can do is checking the node-order to see if they obviously differ (by size of node's name).
     *
     * if this function returns true, it's _NOT_ a garanty that both graphs are compatibles,
     * for exemple the state transition graph could have higher max value for one of the node.
     *
     * @param regGraph
     * @param dynHieGraph
     * @return true if the two graph can be associated
     */
    public static boolean associationValid(RegulatoryGraph regGraph, DynamicGraph dynGraph) {
        if (regGraph == null || dynGraph == null) {
            return false;
        }

        List regOrder = regGraph.getNodeOrder();
        List dynOrder = dynGraph.getNodeOrder();
        if (regOrder == null || dynOrder == null || regOrder.size() != dynOrder.size()) {
            return false;
        }

        for (int i=0 ; i<regOrder.size() ; i++) {
            if (!dynOrder.get(i).equals( regOrder.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public OMDDNode[] getAllTrees(boolean focal) {
    	return getAllTrees(null, focal);
    }
    
    @Override
    public OMDDNode[] getAllTrees(List<RegulatoryNode> tmpNodeOrder, boolean focal) {
    	if (tmpNodeOrder == null)
    		tmpNodeOrder = nodeOrder;
        OMDDNode[] t_tree = new OMDDNode[tmpNodeOrder.size()];
        for (int i = 0; i < tmpNodeOrder.size(); i++) {
        	RegulatoryNode vertex = (RegulatoryNode)tmpNodeOrder.get(i);
            t_tree[i] = vertex.getTreeParameters(this, tmpNodeOrder);
            if (!focal) {
            	// FIXME: does non-focal tree works correctly ??????
            	t_tree[i] = t_tree[i].buildNonFocalTree(i, vertex.getMaxValue()+1).reduce();
            } else {
            	t_tree[i] = t_tree[i].reduce();
            }
        }
        return t_tree;
    }

    @Override
    public MDDManager getMDDFactory() {
    	return  getMDDFactory(null);
    }

    /**
     * Build a MDDManager with a custom variable order
     * 
     * @param order the desired order or null to use the default order
     * 
     * @return a new MDDManager with the required order
     */
    private MDDManager getMDDFactory(List<NodeInfo> order) {
    	if (order == null) {
    		// default to the node order
    		order = getNodeInfoOrder();
    	}
    	MDDVariableFactory vbuilder = new MDDVariableFactory();
    	for (NodeInfo ni: order) {
    		vbuilder.add(ni, (byte)(ni.getMax()+1));
    	}
    	MDDManager factory = MDDManagerFactory.getManager(vbuilder, 10);
    	return factory;
    	
    }
    
    private List<NodeInfo> getNodeInfoOrder() {
		List<NodeInfo> order = new ArrayList<NodeInfo>();
		for (RegulatoryNode node: getNodeOrder()) {
			order.add(node.getNodeInfo());
		}
		return order;
    }
    
    @Override
    public int[] getMDDs(MDDManager factory) {
    	int[] mdds = new int[getNodeCount()];
    	int i=0;
    	for (RegulatoryNode node: getNodeOrder()) {
    		mdds[i++] = node.getMDD(this, factory);
    	}
    	return mdds;
    }
    
    @Override
	public List<RegulatoryNode> getNodeOrderForSimulation() {
		return getNodeOrder();
	}
	
    @Override
	public OMDDNode[] getParametersForSimulation(boolean focal) {
		
		return getAllTrees(focal);
	}


	@Override
	public LogicalModel getModel() {
		return getModel(null);
	}

	@Override
	public LogicalModel getModel(NodeOrderer orderer) {
		List<NodeInfo> order = null;
		if (orderer == null) {
			order = new ArrayList<NodeInfo>();
			for (RegulatoryNode node: getNodeOrder()) {
				order.add(node.getNodeInfo());
			}
		} else {
			order = orderer.getOrder(this);
		}
		
		MDDManager factory = getMDDFactory(order);
		int[] functions = getMDDs(factory);
		return new LogicalModelImpl(order, factory, functions);
	}

	private List<NodeInfo> getNodeInfos(List<RegulatoryNode> order) {
		List<NodeInfo> n_info = new ArrayList<NodeInfo>(order.size());
		for (RegulatoryNode node: order) {
			n_info.add(node.getNodeInfo());
		}
		return n_info;
	}
	
	@Override
	public List<RegulatoryNode> searchNodes( String regexp) {
		if (!regexp.startsWith("::")) {
			return super.searchNodes(regexp);
		}
		
		regexp = regexp.substring(2);
		List<RegulatoryNode> v = new ArrayList<RegulatoryNode>();
		
		Pattern pattern = Pattern.compile(regexp, Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);
		
		for (RegulatoryNode vertex: getNodes()) {
			Matcher matcher = pattern.matcher(vertex.toString());
			if (matcher.find()) {
				v.add(vertex);
				continue;
			}

			matcher = pattern.matcher(vertex.getName());
			if (matcher.find()) {
				v.add(vertex);
				continue;
			}
			
			Annotation annot = vertex.getAnnotation();
			for (AnnotationLink link: annot.getLinkList()) {
				matcher = pattern.matcher(link.getLink());
				if (matcher.find()) {
					v.add(vertex);
					break;
				}
			}
		}
		return v;
	}

}
