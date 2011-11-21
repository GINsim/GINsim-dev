package org.ginsim.graph.reducedgraph;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ginsim.exception.GsException;
import org.ginsim.graph.GraphManager;
import org.ginsim.graph.common.AbstractDerivedGraph;
import org.ginsim.graph.common.Edge;
import org.ginsim.graph.common.Graph;
import org.ginsim.graph.common.VertexAttributesReader;
import org.ginsim.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.graph.regulatorygraph.RegulatoryVertex;
import org.ginsim.gui.service.tool.connectivity.ReducedParameterPanel;
import org.ginsim.io.parser.GsGinmlHelper;

import fr.univmrs.tagc.common.managerresources.Translator;
import fr.univmrs.tagc.common.xml.XMLWriter;

public class ReducedGraphImpl  extends AbstractDerivedGraph<NodeReducedData, Edge<NodeReducedData>, RegulatoryGraph, RegulatoryVertex, RegulatoryMultiEdge>
	implements ReducedGraph{

	public static final String GRAPH_ZIP_NAME = "connectedComponent.ginml";
	
	private ReducedParameterPanel parameterPanel = null;

    
	/**
	 * @param parent
	 */
	public ReducedGraphImpl( Graph parent) {
		
	    this( false);
        setAssociatedGraph( (RegulatoryGraph) parent);
	}

	/**
	 * @param map
	 * @param file
	 */
	public ReducedGraphImpl(Map map, File file) {
		
	    this( true);
        ReducedGraphParser parser = new ReducedGraphParser();
        parser.parse(file, map, this);
	}

	/**
	 * @param filename
	 */
	public ReducedGraphImpl( boolean parsing) {
		
        super( parsing);
	}
	/**
     * 
     */
    public ReducedGraphImpl() {
    	
        this( false);
    }

    
	/**
	 * Return the zip extension for the graph type
	 * 
	 * @return the zip extension for the graph type
	 */
    @Override
	public String getGraphZipName(){
		
		return GRAPH_ZIP_NAME;
		
	}
    
    /**
     * Return 0 since no node order is defined on this king of graph
     * 
     * @return 0 since no node order is defined on this king of graph
     */
    @Override
	public int getNodeOrderSize(){
		
		return 0;
	}
    
    /**
     * Indicates if the given graph can be associated to the current one
     * 
     * @param graph the graph to associate to the current one
     * @return true is association is possible, false if not
     */
    @Override
    protected boolean isAssociationValid( Graph<?, ?> graph) {
    	
    	if( graph instanceof RegulatoryGraph){
    		return true;
    	}
    	
    	return false;
    }

    @Override
	protected void doSave(OutputStreamWriter os, Collection<NodeReducedData> vertices, Collection<Edge<NodeReducedData>> edges, int mode) throws GsException {
        try {
            XMLWriter out = new XMLWriter(os, GsGinmlHelper.DEFAULT_URL_DTD_FILE);
	  		out.write("<gxl xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
			out.write("\t<graph id=\"" + graphName + "\"");
			out.write(" class=\"reduced\">\n");
			saveVertices(out, mode, vertices);
			saveEdge(out, mode, edges);
            if (graphAnnotation != null) {
            	graphAnnotation.toXML(out, null, 0);
            }
            // save the ref of the associated regulatory graph!
            if (associatedGraph != null) {
                associatedID = GraphManager.getInstance().getGraphPath( associatedGraph);
            }
            if (associatedID != null) {
                out.write("<link xlink:href=\""+associatedID+"\"/>\n");
            }
	  		out.write("\t</graph>\n");
	  		out.write("</gxl>\n");
        } catch (IOException e) {
            throw new GsException(GsException.GRAVITY_ERROR, Translator.getString("STR_unableToSave")+": " +e.getMessage());
        }
	}

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveEdge(XMLWriter out, int mode, Collection<Edge<NodeReducedData>> edges) throws IOException {
        Iterator<Edge<NodeReducedData>> it;
    	if (edges == null) {
    		edges = getEdges();
    	}
        switch (mode) {
        	default:
		        for (Edge<NodeReducedData> edge: edges) {
		            NodeReducedData source = edge.getSource();
		            NodeReducedData target = edge.getTarget();
		            out.write("\t\t<edge id=\""+ source +"_"+target+"\" from=\""+source+"\" to=\""+target+"\"/>\n");
		        }
		        break;
        }
    }

    /**
     * @param out
     * @param mode
     * @param selectedOnly
     * @throws IOException
     */
    private void saveVertices(XMLWriter out, int mode, Collection<NodeReducedData> vertices) throws IOException {
    	Iterator it;
    	if (vertices == null) {
    		vertices = getVertices();
    	}
    	VertexAttributesReader vReader = getVertexAttributeReader();
    	switch (mode) {
    		case 1:

                for (NodeReducedData vertex: vertices) {
                    String content = vertex.getContentString();
                    out.write("\t\t<node id=\""+vertex+"\">\n");
                    out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
                    out.write(GsGinmlHelper.getShortNodeVS(vReader));
                    out.write("\t\t</node>\n");
                }
    			break;
			case 2:
                for (NodeReducedData vertex: vertices) {
                    vReader.setVertex(vertex);
                    String content = ((NodeReducedData)vertex).getContentString();
                    out.write("\t\t<node id=\""+vertex+"\">\n");
                    out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
                    out.write(GsGinmlHelper.getFullNodeVS(vReader));
                    out.write("\t\t</node>\n");
                }
    			break;
    		default:
                for (NodeReducedData vertex: vertices) {
                    String content = vertex.getContentString();
                    out.write("\t\t<node id=\""+vertex+"\">\n");
                    out.write("<attr name=\"content\"><string>"+content+"</string></attr>");
                    out.write("</node>");
    	        }
        }
    }
	
	/**
	 * add an edge to this graph.
	 * @param source source vertex of this edge.
	 * @param target target vertex of this edge.
	 */
    @Override
	public void addEdge(NodeReducedData source, NodeReducedData target) {
		Edge<NodeReducedData> edge = new Edge<NodeReducedData>(source, target);
		addEdge( edge);
	}
	
    @Override
    protected List doMerge(Graph otherGraph) {
        return null;
    }
    
    @Override
    public Graph getSubgraph(Collection vertex, Collection edges) {
        // no copy for reduced graphs
        return null;
    }

    /**
     * @return a map referencing all real nodes in the selected CC
     */
    @Override
    public Map getSelectedMap(Collection<NodeReducedData> selection) {
        Map map = new HashMap();
        for (NodeReducedData node: selection) {
            Vector content = node.getContent();
            for (int i=0 ; i<content.size() ; i++) {
                map.put(content.get(i).toString(), null);
            }
        }
        return map;
    }
}
