package fr.univmrs.ibdm.GINsim.stableStates;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import fr.univmrs.ibdm.GINsim.manageressources.Translator;
import fr.univmrs.ibdm.GINsim.reg2dyn.GsMutantModel;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsMutantListManager;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryMutants;
import fr.univmrs.ibdm.GINsim.util.widget.GsDialog;

public class GsStableStateUI extends GsDialog {
	private static final long serialVersionUID = -3605525202652679586L;

	
	GsRegulatoryGraph graph;
	JComboBox comboMutant;
	GsMutantModel mutantmodel;
	
	public GsStableStateUI(GsRegulatoryGraph graph) {
		super(graph.getGraphManager().getMainFrame(), "display.stableStates", 200, 100);
		this.graph = graph;
		Container panel = getContentPane();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		mutantmodel = new GsMutantModel((GsRegulatoryMutants)graph.getObject(GsMutantListManager.key, false));
		comboMutant = new JComboBox(mutantmodel);
		panel.add(comboMutant, c);
		c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		JButton butRun = new JButton(Translator.getString("STR_run"));
		panel.add(butRun, c);
		butRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doStart();
			}
		});
	}
	
	protected void doStart() {
		new GsSearchStableStates(graph, mutantmodel.getMutant()).start();
	}
	
	public void doClose() {
		setVisible(false);
		dispose();
	}
}
