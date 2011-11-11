package org.ginsim.gui.service.action.stablestates;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.ginsim.service.action.stablestates.StableStatesService;

import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.GINsim.regulatoryGraph.OmddNode;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.GsRegulatoryMutantDef;
import fr.univmrs.tagc.GINsim.regulatoryGraph.mutant.MutantSelectionPanel;
import fr.univmrs.tagc.common.datastore.ObjectStore;
import fr.univmrs.tagc.common.managerresources.Translator;
import fr.univmrs.tagc.common.widgets.EnhancedJTable;
import fr.univmrs.tagc.common.widgets.StackDialog;

public class GsStableStateUI extends StackDialog implements GenericStableStateUI {
	private static final long serialVersionUID = -3605525202652679586L;
	
	GsRegulatoryGraph graph;
	StableTableModel tableModel;
	ObjectStore mutantstore = new ObjectStore();
	MutantSelectionPanel mutantPanel;
	JPanel buttonPanel;
	
	public GsStableStateUI(GsRegulatoryGraph graph) {
		super(graph, "display.stableStates", 200, 100);
		this.graph = graph;
		setTitle(Translator.getString("STR_stableStates"));

		Container panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel(Translator.getString("STR_stableStates_title")), c);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mutantPanel = new MutantSelectionPanel(this, graph, mutantstore);
		panel.add(mutantPanel, c);

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		JScrollPane sp = new JScrollPane();
		tableModel = new StableTableModel(graph.getNodeOrder(), false);

        EnhancedJTable tableResult = new EnhancedJTable(tableModel);
        tableResult.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableResult.getTableHeader().setReorderingAllowed(false);
		sp.setViewportView(tableResult);
		panel.add(sp, c);
		
		setMainPanel(panel);
	}
	
	protected void run() {
		setRunning(true);
		// Pedro: I changed both this file and StableStatesService.java to separate
		// GUI/Service and use Service on NuSMVExport. Also removed Thread from GsSearchSS.
		StableStatesService sss = new StableStatesService(graph,
				graph.getNodeOrder(),
				(GsRegulatoryMutantDef)mutantstore.getObject(0));
		setResult(sss.getStable());
	}
	
	public void setResult(OmddNode stable) {
		tableModel.setResult(stable, graph);
		setRunning(false);
	}
	
	public void doClose() {
		setVisible(false);
		dispose();
	}
}
