package fr.univmrs.tagc.GINsim.export.regulatoryGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.ginsim.exception.GsException;
import org.ginsim.graph.common.Graph;
import org.ginsim.graph.regulatorygraph.GsRegulatoryGraph;
import org.ginsim.graph.regulatorygraph.GsRegulatoryMultiEdge;
import org.ginsim.graph.regulatorygraph.GsRegulatoryVertex;

import fr.univmrs.tagc.GINsim.export.GsAbstractExport;
import fr.univmrs.tagc.GINsim.export.GsExportConfig;
import fr.univmrs.tagc.GINsim.graph.GsEdgeAttributesReader;
import fr.univmrs.tagc.GINsim.graph.GsVertexAttributesReader;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutants;
import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.xml.XMLWriter;

/**
 * CytoscapeExport is a plugin for GINsim to export a regulatory graph into XGMML format.
 * 
 * @author BERENGUIER duncan - M1BBSG
 * @version 1.0
 * february 2008 - april 2008 
 */
public class CytoscapeExport extends GsAbstractExport<Graph<?,?>> {
	
	int EDGE_INHIBIT = 1;
	int EDGE_ACTIVATE = 2;
	int EDGE_UNDEFINED = 3;
	GsExportConfig config = null;
	GsRegulatoryMutants mlist = null;
	FileWriter fout = null;
	XMLWriter out = null;
	
	public CytoscapeExport(Graph graph) {
		super(graph, "STR_cytoscape", "STR_cytoscape_descr");
		extension = ".xgmml";
		filter = new String[] { "xgmml" };
		filterDescr = "Cytoscape files";
	}

	protected void doExport(GsExportConfig config) throws GsException{
		
		this.config = config;
		try {
			run();
		} catch (IOException e) {
			throw new GsException(GsException.GRAVITY_ERROR, e);
		}
	}
	
	/**
	 * Write the regulatory graph into a file using XMLWriter.
	 * @throws IOException
	 */
	protected synchronized void run() throws IOException {
		
		GsRegulatoryGraph graph = (GsRegulatoryGraph) config.getGraph();
		fout = new FileWriter(config.getFilename());
		out = new XMLWriter(fout, null);
				
		//Header
		out.write("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
		out.openTag("graph");
		out.addAttr("label", graph.getGraphName());
		out.addAttr("id", graph.getGraphName());
		out.addAttr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		out.addAttr("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		out.addAttr("xmlns:xlink", "http://www.w3.org/1999/xlink");
		out.addAttr("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		out.addAttr("xmlns", "http://www.cs.rpi.edu/XGMML");
		
		out.addTag("att", new String[] {"name", "documentVersion", "value", "1.0"});

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//for dc:date
		out.openTag("att");
		out.addAttr("name", "networkMetadata");
		out.openTag("rdf:RDF");
		out.openTag("rdf:Description");
		out.addAttr("rdf:about", "http://www.cytoscape.org/");
		out.addTagWithContent("dc:type", "Protein-Protein Interaction");
		out.addTagWithContent("dc:description", graph.getAnnotation().getHTMLComment());
		out.addTagWithContent("dc:identifier", "N/A");
		out.addTagWithContent("dc:date", simpleDateFormat.format(new Date()).toString());
		out.addTagWithContent("dc:title",  graph.getGraphName());
		out.addTagWithContent("dc:source", "http://www.cytoscape.org/");
		out.addTagWithContent("dc:format", "Cytoscape-XGMML");
		out.closeTag();//Description
		out.closeTag();//RDF
		out.closeTag();//Att
		
		out.addTag("att", new String[] {"name", "backgroundColor", "value", "#ffffff"});
		out.addTag("att", new String[] {"name", "GRAPH_VIEW_ZOOM", "label", "GRAPH_VIEW_ZOOM", "type", "real", "value", "1.0"});
		out.addTag("att", new String[] {"name", "GRAPH_VIEW_CENTER_X", "label", "GRAPH_VIEW_CENTER_X", "type", "real", "value", "0.0"});
		out.addTag("att", new String[] {"name", "GRAPH_VIEW_CENTER_Y", "label", "GRAPH_VIEW_CENTER_Y", "type", "real", "value", "0.0"});

		
		//vertex
		//We need a Hashtable to translate GINSim IDs into cytoscapes IDs.
		Hashtable gs2cyt_Ids = new Hashtable(graph.getVertexCount());
		
		int current_index_of_node_id = -graph.getVertexCount(); // The IDs goes from -vertexCount to -1
		GsVertexAttributesReader vertexAttributeReader = graph.getVertexAttributeReader();
		for (Iterator it=graph.getVertices().iterator() ; it.hasNext() ;) {
			GsRegulatoryVertex vertex = (GsRegulatoryVertex)it.next();
			
			String name = vertex.getName();//The complete name (label) of the edge
			if (name.length() == 0) {
				name = vertex.getId(); //if it isn't defined, set to id
			}
			
			Integer node_id = new Integer(current_index_of_node_id++); //Current cytoscape ID
			gs2cyt_Ids.put(vertex.getId(), node_id);//Put the new ID into the map
			
			out.openTag("node");
			out.addAttr("label", vertex.getId());
			out.addAttr("id", node_id.toString());
		
			out.addTag("att", new String[] {"name", "hiddenLabel", "label", "hiddenLabel", "type", "string"});
			out.addTag("att", new String[] {"name", "canonicalName", "label", "canonicalName", "type", "string", "value", name});
			
			vertexAttributeReader.setVertex(vertex);
			out.openTag("graphics");
			out.addAttr("w", String.valueOf(vertexAttributeReader.getWidth()));
			out.addAttr("h", String.valueOf(vertexAttributeReader.getHeight()));
			out.addAttr("width", "1");
			out.addAttr("outline", '#'+Tools.getColorCode(vertexAttributeReader.getForegroundColor()));
			out.addAttr("fill", '#'+Tools.getColorCode(vertexAttributeReader.getBackgroundColor()));
			out.addAttr("y", String.valueOf(vertexAttributeReader.getY()));
			out.addAttr("x", String.valueOf(vertexAttributeReader.getX()));
			if (vertexAttributeReader.getShape() == GsVertexAttributesReader.SHAPE_RECTANGLE) {
				out.addAttr("type", "rectangle");
			} else if (vertexAttributeReader.getShape() == GsVertexAttributesReader.SHAPE_ELLIPSE) {
				out.addAttr("type", "ellipse");
			}			
			out.openTag("att");
			out.addAttr("name", "cytoscapeNodeGraphicsAttributes");
			out.addTag("att", new String[] {"name", "nodeTransparency", "value", "1.0"});
			out.addTag("att", new String[] {"name", "nodeLabelFont", "value", "Default-0-12"});
			out.addTag("att", new String[] {"name", "borderLineType", "value", "solid"});
			out.closeTag();//att
			out.closeTag();//graphics


			out.closeTag();//Node
		}
		
		//edges
		GsEdgeAttributesReader edgeAttributeReader = graph.getEdgeAttributeReader();
		for (Iterator<GsRegulatoryMultiEdge> it=graph.getEdges().iterator() ; it.hasNext() ;) {
			GsRegulatoryMultiEdge edge = it.next();

			String source_id = ((GsRegulatoryVertex)edge.getTarget()).getId(); //C1
			String target_id = ((GsRegulatoryVertex)edge.getSource()).getId(); //C2
			String edge_type; //inhibit | activate | undefined
			String edge_cyt_id; //15 | 3 | 12
			switch (edge.getSign()) {
				case GsRegulatoryMultiEdge.SIGN_NEGATIVE: 
					edge_type = "inhibit";
					edge_cyt_id = "15";
					break;
				case GsRegulatoryMultiEdge.SIGN_POSITIVE: 
					edge_type = "activate"; 
					edge_cyt_id = "3";
					break;
				default:
					edge_type = "undefined"; 
					edge_cyt_id = "12";
					break;
			}			
			String long_label = source_id+" ("+edge_type+") "+target_id; //C1 (inhibit) C2
			
			edgeAttributeReader.setEdge(edge);
			out.openTag("edge");
			out.addAttr("target", gs2cyt_Ids.get(source_id).toString());
			out.addAttr("source", gs2cyt_Ids.get(target_id).toString());
			out.addAttr("label", long_label);
			out.addAttr("id", long_label);
			
	        out.addTag("att", new String[] {"name", "XGMML Edge Label", "label", "XGMML Edge Label", "type", "string", "value", long_label});
			out.addTag("att", new String[] {"name", "interaction", "label", "interaction", "type", "string", "value", edge_type});
			out.addTag("att", new String[] {"name", "canonicalName", "label", "canonicalName", "type", "string", "value", long_label});

			out.openTag("graphics");
			out.addAttr("width", String.valueOf((int)edgeAttributeReader.getLineWidth()));
			out.addAttr("fill", '#'+Tools.getColorCode(edgeAttributeReader.getLineColor()));
			out.openTag("att");
			out.addAttr("name", "cytoscapeEdgeGraphicsAttributes");
			out.addTag("att", new String[] {"name", "sourceArrow", "value", "0"});
			out.addTag("att", new String[] {"name", "targetArrow", "value", edge_cyt_id});
			out.addTag("att", new String[] {"name", "edgeLabelFont", "value", "Default-0-10"});
			out.addTag("att", new String[] {"name", "edgeLineType", "value", "SOLID"});
			out.addTag("att", new String[] {"name", "sourceArrowColor", "value", '#'+Tools.getColorCode(edgeAttributeReader.getLineColor())});
			out.addTag("att", new String[] {"name", "targetArrowColor", "value", '#'+Tools.getColorCode(edgeAttributeReader.getLineColor())});
			if (edgeAttributeReader.getStyle() == GsEdgeAttributesReader.STYLE_STRAIGHT) {
				out.addTag("att", new String[] {"name", "curved", "value", "STRAIGHT_LINES"});
			} else {
				out.addTag("att", new String[] {"name", "curved", "value", "CURVED_LINES"});
			}
			out.closeTag();//att
			out.closeTag();//graphics

			out.closeTag();//edge
		}
		
		//End
		out.closeTag();//graph
		fout.close(); //Close filewriter
	}
}
