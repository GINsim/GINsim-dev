package org.ginsim.service.export.biolayout;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.Graph;

public class BiolayoutEncoder {


	/**
	 * Encode the graph to biolayout output
	 * 
	 * @param graph the graph to encode
	 * @param edges the list of edges that must be part of the output
	 * @param filename the path to the output file
	 */
	public void encode( Graph graph, Collection<Edge<?>> edges, String filename) throws IOException{
		
        FileWriter out = new FileWriter(filename);

        // out.write("// Biolayout file generated by GINsim - " + 
        // DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()) +"\n");
        
        // Process Edges
        if (edges == null) {
        	edges = graph.getEdges();
        }
        for (Edge edge: edges) {
    		Object from = edge.getSource();
    		Object to = edge.getTarget();
        	out.write(from + "\t" + to + "\n");
        }

		// Close main tags
		out.close();
	}
}
