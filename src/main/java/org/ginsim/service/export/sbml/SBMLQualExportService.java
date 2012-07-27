package org.ginsim.service.export.sbml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.colomoto.logicalmodel.LogicalModel;
import org.colomoto.logicalmodel.io.sbml.SBMLFormat;
import org.colomoto.logicalmodel.io.sbml.SBMLqualExport;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.mangosdk.spi.ProviderFor;

@ProviderFor( Service.class)
@Alias("SBMLe")
public class SBMLQualExportService implements Service {

	private static boolean USEJSBML = false;
	
	/**
	 * Execute the export by instantiating the right encoder
	 * 
	 * @param graph the graph to export
	 * @param config the configuration structure
	 * @param filename the path to the target file
	 * @throws IOException
	 */
	public void export( RegulatoryGraph graph, SBMLQualConfig config, String filename) throws IOException{
		
		if (USEJSBML) {
			exportJSBML(graph, filename+".jsbml");
		} else {
			SBMLQualEncoder encoder = new SBMLQualEncoder( );
			encoder.doExport( graph, config, filename);
		}
	}
	
	/**
	 * Convenience method to export without having to configure anything
	 * @param graph
	 * @param filename
	 * @throws IOException
	 */
	public void export( RegulatoryGraph graph, String filename) throws IOException {
		SBMLQualConfig cfg = new SBMLQualConfig(graph);
		export(graph, cfg, filename);
	}

	/**
	 * Export using the JSBML encoder for logical models.
	 * 
	 * @param graph
	 * @param filename
	 * @throws IOException
	 */
	public void exportJSBML( RegulatoryGraph graph, String filename) throws IOException {
		LogicalModel model = graph.getModel();
		OutputStream out = new FileOutputStream(new File(filename));
		try {
			new SBMLqualExport(model).export(out);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
}
