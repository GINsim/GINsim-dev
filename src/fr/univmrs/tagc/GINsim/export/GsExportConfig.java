package fr.univmrs.tagc.GINsim.export;

import java.util.Vector;

import org.ginsim.graph.common.Graph;

import fr.univmrs.tagc.GINsim.graph.GsExtensibleConfig;
import fr.univmrs.tagc.common.OptionStore;

public class GsExportConfig<G extends Graph> extends GsExtensibleConfig<G> {

	int format = -1;
	String filename;
	
	public GsExportConfig( G graph, GsAbstractExport export) {
		
		super(graph);

		// set the format
		Vector v_format = export.getSubFormat();
		if (v_format != null) {
			String s_format = (String)OptionStore.getOption("export."+export.getID()+".format", 
					v_format.get(0).toString());
			format = 0;
			for (int i=0 ; i<v_format.size() ; i++) {
				if (s_format.equals(v_format.get(i).toString())) {
					format = i;
					break;
				}
			}
			setFormat(format, export);
		}
	}
		
	public void setFormat(int index, GsAbstractExport export) {
		Vector v_format = export.getSubFormat();
		if (v_format == null) {
			return;
		}
		if (index >-1 && index < v_format.size()) {
			OptionStore.setOption("export."+export.getID()+".format", v_format.get(index).toString());
			format = index;
		}
	}

	public String getFilename() {
		return filename;
	}
	public String setFilename(String filename) {
		return this.filename = filename;
	}
}
