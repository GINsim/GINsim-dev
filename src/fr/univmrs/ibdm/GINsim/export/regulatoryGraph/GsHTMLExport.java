package fr.univmrs.ibdm.GINsim.export.regulatoryGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.table.TableModel;

import fr.univmrs.ibdm.GINsim.export.GsAbstractExport;
import fr.univmrs.ibdm.GINsim.export.GsExportConfig;
import fr.univmrs.ibdm.GINsim.global.GsEnv;
import fr.univmrs.ibdm.GINsim.global.GsException;
import fr.univmrs.ibdm.GINsim.graph.GsGraph;
import fr.univmrs.ibdm.GINsim.gui.GsPluggableActionDescriptor;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsMutantListManager;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.OmddNode;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.initialState.GsInitStateTableModel;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.initialState.GsInitialStateList;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.initialState.GsInitialStateManager;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.mutant.GsRegulatoryMutantDef;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.mutant.GsRegulatoryMutants;
import fr.univmrs.ibdm.GINsim.stableStates.GenericStableStateUI;
import fr.univmrs.ibdm.GINsim.stableStates.GsSearchStableStates;
import fr.univmrs.ibdm.GINsim.stableStates.StableTableModel;

public class GsHTMLExport extends GsAbstractExport implements GenericStableStateUI {

	OmddNode stable = null;
	GsExportConfig config = null;

	public GsPluggableActionDescriptor[] getT_action(int actionType,
			GsGraph graph) {
		if (graph instanceof GsRegulatoryGraph) {
			return new GsPluggableActionDescriptor[] { new GsPluggableActionDescriptor(
					"STR_html", "STR_html_descr", null, this, ACTION_EXPORT, 0) };
		}
		return null;
	}

	protected void doExport(GsExportConfig config) {
		this.config = config;
		new GsSearchStableStates(config.getGraph(), null, this).start();
	}
	
	protected synchronized void run() {
		GsRegulatoryGraph graph = (GsRegulatoryGraph) config.getGraph();
		Vector v_no = graph.getNodeOrder();
		int len = v_no.size();
		try {
			FileWriter out = new FileWriter(config.getFilename());

			out.write("<!DOCTYPE html PUBLIC \""
					+ "-//W3C//DTD XHTML 1.0 Strict//EN"
					+ "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\""
					+ ">\n");
			out.write("<html>\n<head>");
			out.write("<title>\nSave of the model " + graph.getGraphName() + "\n</title>\n");
			out.write("  <style type='text/css'>\n"
					+ "    table {border-collapse: collapse }\n"
					+ "    table td {border: solid ; border-width: 1px }\n"
					+ "  </style>\n");
			out.write("</head>\n");
			out.write("<body>\n");
			
			out.write("  <h1>Data of the model \"" + graph.getGraphName() + "\"</h1>\n");

			TableModel model;
			
			// initial states
			out.write("  <h2>Initial states</h2>\n");
			GsInitialStateList initStates = (GsInitialStateList) graph.getObject(
					GsInitialStateManager.key, false);
			if (initStates != null && initStates.getNbElements(null) > 0) {
				model = new GsInitStateTableModel(v_no, null, initStates, false);
				out.write("  <table>\n");
				out.write("  <tr>\n");
				out.write("    <td>Name</td>\n");
				for (int i = 0; i < len; i++) {
					out.write("<td>" + v_no.get(i) + "</td>\n");
				}
				out.write("</tr>\n");
				for ( int i=0 ; i< initStates.getNbElements(null) ; i++ ) {
					out.write("<tr>\n");
					out.write("<td>" + model.getValueAt(i, 0) + "</td>\n");
					for (int j = 0; j < len; j++) {
						out.write("<td>" + model.getValueAt(i, j+2) + "</td>\n");
					}
				}
				out.write("</tr>\n" + "</table>\n");
			}
			
			// stable states
			out.write("<hr/><h2>Stable States</h2>\n");
			if (stable != null) {
				if (stable == OmddNode.TERMINALS[0]) {
					out.write("No stable state\n");
				} else {
					model = new StableTableModel(v_no);
					((StableTableModel)model).setResult(stable);
					out.write("<table>\n");
					out.write("<tr>\n");
					for (int i = 0; i < len; i++) {
						out.write("<td>" + v_no.get(i) + "</td>\n");
					}
					out.write("</tr>\n");
					for (int i=0 ; i<model.getRowCount() ; i++) {
						out.write("<tr>\n");
						for (int j=0 ; j<len ; j++) {
							out.write("<td>"+model.getValueAt(i,j)+"</td>");
						}
						out.write("</tr>\n");
					}
					out.write("</table>\n");
				}
			}
			
			// mutants
			GsRegulatoryMutants mlist = (GsRegulatoryMutants)graph.getObject(GsMutantListManager.key, false);
			if (mlist != null && mlist.getNbElements(null) > 0) {
				out.write("  <h2>Mutants</h2>\n");
				out.write("<table>\n");
				out.write("<tr align = " + "\"center\"" + ">\n");
				out.write("<td>Mutant</td><td>Gene</td><td>Min</td>\n<td>Max</td>\n</tr>\n");
				for (int i=0 ; i< mlist.getNbElements(null) ; i++) {
					GsRegulatoryMutantDef mutant = (GsRegulatoryMutantDef)mlist.getElement(null, i);
					int nb = mutant.getNbChanges();
					out.write("<tr>\n<td rowspan="+(nb+1)+">" + mutant.getName()+"</td>\n");
					for (int j=0 ; j<nb ; j++) {
							out.write("<tr><td>"
									+ mutant.getName(j)
									+ "</td><td>"+ mutant.getMin(j)
									+ "</td>\n<td>" + mutant.getMax(j)+"</td></tr>\n");
					}
				}
				out.write("</table>\n");
			}
			
			// close the html file
			out.write("</body>\n" + "</html>");
			out.close();
		} catch (IOException e) {
			GsEnv.error(new GsException(GsException.GRAVITY_ERROR, e
					.getLocalizedMessage()), null);
		}
	}

	public void setResult(OmddNode stable) {
		this.stable = stable;
		run();
	}
}