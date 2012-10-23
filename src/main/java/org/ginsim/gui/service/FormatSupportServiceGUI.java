package org.ginsim.gui.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.colomoto.logicalmodel.LogicalModel;
import org.ginsim.common.application.LogManager;
import org.ginsim.common.utils.FileFormatDescription;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.regulatorygraph.LogicalModel2RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.service.FormatSupportService;
import org.ginsim.gui.GUIManager;
import org.ginsim.gui.service.common.ExportAction;
import org.ginsim.gui.service.common.ImportAction;

/**
 * Generic GUI integration service for formats providers.
 * 
 * @author Aurelien Naldi
 *
 * @param <S>
 */
public class FormatSupportServiceGUI<S extends FormatSupportService> extends AbstractServiceGUI {

	private final S service;
	private final String format_name;
	private final FileFormatDescription format;

	protected String import_tip = "";
	protected String export_tip = "";
	
	
	public FormatSupportServiceGUI(String name, S service, FileFormatDescription format) {
		this.format_name = name;
		this.format = format;
		this.service = service;
	}
	
	@Override
	public List<Action> getAvailableActions(Graph<?, ?> graph) {
		List<Action> actions = new ArrayList<Action>();
		if (service.canImport()) {
			actions.add(new FormatImportAction(format_name, import_tip, this));
		}
		if (service.canExport() && graph instanceof RegulatoryGraph) {
			actions.add(new FormatExportAction(format_name, export_tip, (RegulatoryGraph)graph, this));
		}
		return actions;
	}

	@Override
	public int getInitialWeight() {
		return 5;
	}
	
	public void doImport(String filename) {
		if (filename == null) {
			return;
		}

		try {
			LogicalModel model = service.importFile(filename);
			RegulatoryGraph newGraph = LogicalModel2RegulatoryGraph.importModel(model);
			GUIManager.getInstance().whatToDoWithGraph(newGraph, true);
		} catch (IOException e) {
			LogManager.error("Error in "+format_name+" import");
		}
	}
	
	public void doExport(RegulatoryGraph graph, String filename) {
		if (filename == null) {
			return;
		}

		try {
			service.export(graph.getModel(), filename);
		} catch (IOException e) {
			LogManager.error("Error in "+format_name+" export");
		}

	}
	
	public FileFormatDescription getFileFilter() {
		return format;
	}

}


class FormatImportAction extends ImportAction {

	private final FormatSupportServiceGUI serviceGUI;
	
	
	public FormatImportAction(String title, String tooltip, FormatSupportServiceGUI serviceGUI) {
		super(title, tooltip);
		this.serviceGUI = serviceGUI;
	}

	@Override
	protected FileFormatDescription getFileFilter() {
		return serviceGUI.getFileFilter();
	}

	@Override
	protected void doImport(String filename) {
		serviceGUI.doImport(filename);
	}
}

class FormatExportAction extends ExportAction<RegulatoryGraph> {

	private final FormatSupportServiceGUI serviceGUI;

	public FormatExportAction(String title, String tooltip, RegulatoryGraph graph, FormatSupportServiceGUI serviceGUI) {
		super(graph, title, tooltip, serviceGUI);
		this.serviceGUI = serviceGUI;
	}

	@Override
	public FileFormatDescription getFileFilter() {
		return serviceGUI.getFileFilter();
	}

	@Override
	protected void doExport(String filename) throws IOException {
		serviceGUI.doExport(graph, filename);
	}
}
