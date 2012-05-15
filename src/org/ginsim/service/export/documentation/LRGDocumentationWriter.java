package org.ginsim.service.export.documentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ginsim.common.document.DocumentStyle;
import org.ginsim.common.document.DocumentWriter;
import org.ginsim.common.utils.IOUtils;
import org.ginsim.core.annotation.Annotation;
import org.ginsim.core.annotation.AnnotationLink;
import org.ginsim.core.graph.objectassociation.ObjectAssociationManager;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.regulatorygraph.initialstate.GsInitialStateList;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateList;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStateManager;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.LogicalParameter;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.TreeInteractionsModel;
import org.ginsim.core.graph.regulatorygraph.logicalfunction.graphictree.datamodel.TreeValue;
import org.ginsim.core.graph.regulatorygraph.mutant.MutantListManager;
import org.ginsim.core.graph.regulatorygraph.mutant.RegulatoryMutantDef;
import org.ginsim.core.graph.regulatorygraph.mutant.RegulatoryMutants;
import org.ginsim.core.graph.regulatorygraph.omdd.OMDDNode;
import org.ginsim.core.service.ServiceManager;
import org.ginsim.gui.graph.regulatorygraph.initialstate.InitStateTableModel;
import org.ginsim.service.tool.stablestates.StableStateSearcher;
import org.ginsim.service.tool.stablestates.StableStatesService;
import org.ginsim.servicegui.tool.stablestates.StableTableModel;



/**
 * GenericDocumentExport is a plugin to export the documentation of a model into multiples document format.
 * 
 * It export using a documentWriter. You can add support for your own document writer using <u>addSubFormat</u>
 * 
 * @see DocumentWriter
 */
public class LRGDocumentationWriter {

	private DocumentWriter doc;

	private final RegulatoryGraph graph;
	private List nodeOrder;
	private int len;

	private DocumentExportConfig config;
	
    public LRGDocumentationWriter(RegulatoryGraph graph) {
    	this.graph = graph;
    }

	public void export( DocumentExportConfig config, String filename) throws Exception {

		this.config = config;
		this.doc = config.format.getWriter();
		
		doc.setOutput(new File(filename));
		nodeOrder = graph.getNodeOrder();
		len = nodeOrder.size();
		setDocumentProperties();
		setDocumentStyles();
		if (doc.doesDocumentSupportExtra("javascript")) {
			setJavascript();
		}
		writeDocument();
	}

	private void writeDocument() throws Exception {
		doc.startDocument();
		doc.openHeader(1, "Description of the model \"" + graph.getGraphName() + "\"", null);
				
		//Graph annotation
		doc.openHeader(2, "Annotation", null);
		writeAnnotation(graph.getAnnotation());
		doc.openParagraph(null);
		
		// FIXME: add back image to the documentation
		// doc.addImage(graph.getGraphManager().getImage(), "model.png");
		
		doc.closeParagraph();
		
		// all nodes with comment and logical functions
		if (true) {
			doc.openHeader(2, "Nodes", null);
			writeLogicalFunctionsTable(config.putComment);
		}
		
		// initial states
		if (config.exportInitStates) {
			doc.openHeader(2, "Initial States", null);
			writeInitialStates();
		}
		// mutant description
		if (config.exportMutants) {
			doc.openHeader(2, "Mutants and Dynamical Behaviour", null);
			writeMutants();
		}

		doc.close();//close the document		
	}

	private void writeMutants() throws Exception {
		RegulatoryMutants mutantList = (RegulatoryMutants) ObjectAssociationManager.getInstance().getObject(graph, MutantListManager.KEY, true);
		StableStateSearcher stableSearcher = ServiceManager.get(StableStatesService.class).getSearcher(graph);
		OMDDNode stable;
		
		String[] cols;
		if (config.searchStableStates && config.putComment) {
			cols = new String[] {"", "", "", "", "", "", ""};
		} else if (config.searchStableStates || config.putComment){
			cols = new String[] {"", "", "", "", "", ""};
		} else {
			cols = new String[] {"", "", "", "", ""};
		}
		int nbcol = cols.length-1;
		doc.openTable("mutants", "table", cols);
		doc.openTableRow(null);
		doc.openTableCell("Mutants", true);
		doc.openTableCell("Gene", true);
		doc.openTableCell("Min", true);
        doc.openTableCell("Max", true);
        doc.openTableCell("Condition", true);
		if (config.putComment) {
			doc.openTableCell("Comment", true);
		}
		if (config.searchStableStates) {
			doc.openTableCell("Stable States", true);
		}
		
		StableTableModel model = new StableTableModel(nodeOrder);
		for (int i=-1 ; i<mutantList.getNbElements(null) ; i++) {
			RegulatoryMutantDef mutant = null;
			Object perturbation  = null;
			if (i >= 0) {
				perturbation = mutantList.getElement(null, i);
			}
			if (perturbation instanceof RegulatoryMutantDef) {
				mutant = i<0 ? null : (RegulatoryMutantDef)perturbation;
			} else {
				// TODO: find a way to document all perturbation types
				continue;
			}
			
			if (config.searchStableStates) {
				stableSearcher.setPerturbation(mutant);
				stable = stableSearcher.call();
				model.setResult(stable, graph);
			}
			int nbrow;
			Iterator it_multicellularChanges = null;
			if (i<0) { // wild type
				nbrow = 1;
				doc.openTableRow(null);
				doc.openTableCell(1, (model.getRowCount() > 0?2:1), "Wild Type", true);
				doc.openTableCell("-");
				doc.openTableCell("-");
                doc.openTableCell("-");
                doc.openTableCell("");
				if (config.putComment) {
					doc.openTableCell("");
				}
			} else {
				if (!config.multicellular) {
					nbrow = mutant.getNbChanges();
				} else {
					nbrow = mutant.getNbChanges();
					Map m_multicellularChanges = new HashMap();
					for (int c=0 ; c<nbrow ; c++) {
						String s = mutant.getName(c);
						// TODO: check that the mutant is indeed the same everywhere before doing so ?
						if (s.endsWith("1")) {
							m_multicellularChanges.put(s.substring(0, s.length()-1), 
									new int[] {mutant.getMin(c), mutant.getMax(c)});
						}
					}
					nbrow = m_multicellularChanges.size();
					it_multicellularChanges = m_multicellularChanges.entrySet().iterator();
				}
				if (nbrow < 1) {
					nbrow = 1;
				}
				doc.openTableRow(null);
				doc.openTableCell(1, (nbrow+(model.getRowCount() > 0?1:0)), mutant.getName(), true);
				if (mutant.getNbChanges() == 0) {
					doc.openTableCell("-");
					doc.openTableCell("-");
                    doc.openTableCell("-");
				} else if (it_multicellularChanges == null){
					doc.openTableCell(mutant.getName(0));
					doc.openTableCell(""+mutant.getMin(0));
					doc.openTableCell(""+mutant.getMax(0));
				} else {
					Entry e = (Entry)it_multicellularChanges.next();
					doc.openTableCell(e.getKey().toString());
					int[] t_changes = (int[])e.getValue();
					doc.openTableCell(""+t_changes[0]);
					doc.openTableCell(""+t_changes[1]);
				}
				if (mutant.getNbChanges() > 0) {
				    doc.openTableCell(mutant.getCondition(0));
				} else {
				    doc.openTableCell("");
				}
				if (config.putComment) {
					doc.openTableCell(1, nbrow, "", false);
					writeAnnotation(mutant.getAnnotation());//BUG?
				}
			}
			
			if (config.searchStableStates) {
				// the common part: stable states
				if (model.getRowCount() > 0) {
					doc.openTableCell(1, nbrow, model.getRowCount()+" Stable states", false);
				} else {
					doc.openTableCell(1, nbrow, "", false);
				}
			}

			// more data on mutants:
			if (mutant != null) {
				for (int j=1 ; j<nbrow ; j++) {
					if (it_multicellularChanges == null) {
						doc.openTableRow(null);
						doc.openTableCell(mutant.getName(j));
						doc.openTableCell(""+mutant.getMin(j));
						doc.openTableCell(""+mutant.getMax(j));
					} else {
						Entry e = (Entry)it_multicellularChanges.next();
						doc.openTableRow(null);
						doc.openTableCell(e.getKey().toString());
						int[] t_changes = (int[])e.getValue();
						doc.openTableCell(""+t_changes[0]);
						doc.openTableCell(""+t_changes[1]);
					}
                    doc.openTableCell(""+mutant.getCondition(j));
				}
			}
			
			// more data on stable states:
			if (config.searchStableStates && model.getRowCount() > 0) {
				doc.openTableRow(null);
				doc.openTableCell(nbcol,1, null, false);
				
				doc.openList("L1");
				for (int k=0 ; k<model.getRowCount() ; k++) {
					doc.openListItem(null);
					boolean needPrev=false;
					String name = (String)model.getValueAt(k,0);
					if (name != null) {
						doc.writeText(name+": ");
					}
					for (int j=1 ; j<=len ; j++) {
						Object val = model.getValueAt(k,j);
						if (!val.toString().equals("0")) {
							String s = needPrev ? " ; " : "";
							needPrev = true;
							if (val.toString().equals("1")) {
								doc.writeText(s+nodeOrder.get(j-1));
							} else {
								doc.writeText(s+nodeOrder.get(j-1)+"="+val);
							}
						}
					}
					doc.closeListItem();
				}
				doc.closeList();
			}
		}
		doc.closeTable();
	}
	
	
	private void writeInitialStates() throws IOException {
		InitialStateList initStates = ((GsInitialStateList) ObjectAssociationManager.getInstance().getObject(graph, 
				InitialStateManager.KEY, false)).getInitialStates();
		if (initStates != null && initStates.getNbElements(null) > 0) {
			InitStateTableModel model = new InitStateTableModel(null, initStates, false);
			String[] t_cols = new String[len+1];
			for (int i=0 ; i<=len ; i++) {
				t_cols[i] = "";
			}
			doc.openTable("initialStates", "table", t_cols);
			doc.openTableRow(null);
			doc.openTableCell("Name");
			for (int i = 0; i < len; i++) {
				doc.openTableCell(""+nodeOrder.get(i));
			}
			for ( int i=0 ; i< initStates.getNbElements(null) ; i++ ) {
				doc.openTableRow(null);
				doc.openTableCell(""+model.getValueAt(i, 0));
				for (int j = 2; j < model.getColumnCount(); j++) {
					doc.openTableCell(""+model.getValueAt(i, j));
				}
			}
			doc.closeTable();
		}
	}

	private void writeLogicalFunctionsTable(boolean putcomment) throws IOException {
		if (config.putComment) {
			doc.openTable(null, "table", new String[] { "", "", "", "" });
		} else {
			doc.openTable(null, "table", new String[] { "", "", "" });
		}
		doc.openTableRow(null);
		doc.openTableCell("ID", true);
		doc.openTableCell("Val", true);
		doc.openTableCell("Logical function", true);
		if (putcomment) {
			doc.openTableCell("Comment", true);
		}
		
		for (RegulatoryNode vertex: graph.getNodeOrder()) {
			TreeInteractionsModel lfunc = vertex.getInteractionsModel();
			int nbval = 0;
			Object funcRoot = null;
			List[] t_val = new List[vertex.getMaxValue()+1];
			int nbrows = 0;
			if (lfunc != null) {
				funcRoot = lfunc.getRoot();
				nbval = lfunc.getChildCount(funcRoot);
				if (nbval == 0) {
					funcRoot = null;
				}
				// put all values from function
				for (int i=0 ; i<nbval ; i++) {
					TreeValue val = (TreeValue)lfunc.getChild(funcRoot, i);
					int v = val.getValue();
					if (lfunc.getChildCount(val) > 0) {
						t_val[v] = new ArrayList();
						t_val[v].add(val);
						nbrows++;
					}
				}
			}
			// and add logical parameters as well
			Iterator it_param = vertex.getV_logicalParameters().iterator(true);
			while (it_param.hasNext()) {
				LogicalParameter param = (LogicalParameter)it_param.next();
				if (!param.isDup()) {
					int v = param.getValue();
					if (t_val[v] == null) {
						t_val[v] = new ArrayList();
						nbrows++;
					}
					t_val[v].add(param);
				}
			}
			doc.openTableRow(null);
			doc.openTableCell(1, nbrows, vertex.getId(), true); //ID
			int currentValue = 0;
			if (nbrows > 0) {
				// TODO: put a "0" if nothing is defined
				for ( ; currentValue<t_val.length ; currentValue++) {
					if (t_val[currentValue] != null) {
						doWriteParameters(currentValue, t_val[currentValue], lfunc);
						break;
					}
				}
			} else {
				doc.openTableCell(null);//Values (empty)
				doc.openTableCell("no function");//function
			}
			if (putcomment) {
				doc.openTableCell(1,nbrows, null, false);
				writeAnnotation(vertex.getAnnotation());
				
				boolean hasRegulatorComment = false;
				for (RegulatoryMultiEdge me: graph.getIncomingEdges(vertex)) {
					if (me.getSource() == vertex) {
						continue;
					}
					Annotation annot = me.getAnnotation();
					if (annot.isEmpty()) {
						continue;
					}
					if (!hasRegulatorComment) {
						doc.openTable("", "", new String[] {"", ""});
						doc.openTableCell("Regulator", true);
						doc.openTableCell("Comment", true);
						hasRegulatorComment = true;
					}
					doc.openTableRow();
					doc.openTableCell(me.getSource().toString(), true);
					doc.openTableCell(null);
					writeAnnotation(me.getAnnotation());
				}
				if (hasRegulatorComment) {
					doc.closeTable();
				}
			}
			doc.closeTableRow();
			
			// add the other functions
			if (nbrows > 1) {
				for (currentValue++ ; currentValue<t_val.length ; currentValue++) {
					if (t_val[currentValue] != null) {
						doc.openTableRow(null);
						doWriteParameters(currentValue, t_val[currentValue], lfunc);
						doc.closeTableRow();
					}
				}
			} 
		}		
		doc.closeTable();		
	}
	
	private void doWriteParameters(int value, List data, TreeInteractionsModel lfunc) throws IOException {
		doc.openTableCell(""+value); //Values
		doc.openTableCell(null); //logical function
		doc.openList("L1");
		for (Iterator it_all=data.iterator() ; it_all.hasNext() ; ) {
			Object o = it_all.next();
			if (o instanceof TreeValue) {
				int nbfunc = lfunc.getChildCount(o);
				for (int j=0 ; j<nbfunc ; j++) {
					Object func = lfunc.getChild(o, j);
					doc.openListItem(func.toString());
				}
			} else {
				doc.openListItem(o.toString());
			}
		}
		doc.closeList();
		doc.closeTableCell();
	}
	
	private void writeAnnotation(Annotation annotation) throws IOException {
		boolean hasLink = false;
		List<AnnotationLink> links = annotation.getLinkList();
		if (links.size() > 0) {
			hasLink = true;
			doc.openList("L1");
		}
		for (AnnotationLink lnk: links) {
			String s_link;
			if (lnk.getHelper() != null) {
				s_link = lnk.getHelper().getLink(lnk.getProto(), lnk.getValue());
			} else {
				s_link = IOUtils.getLink(lnk.getProto(), lnk.getValue());
			}
			if (s_link == null) {
				doc.openListItem(null);
				doc.writeText(lnk.toString());
				doc.closeListItem();
			} else {
				if (s_link == lnk.toString() && s_link.length() >= 50) {
					doc.openListItem(null);
					doc.addLink(s_link, s_link.substring(0, 45) + "...");
					doc.closeListItem();
				} else {
					doc.openListItem(null);
					doc.addLink(s_link, lnk.toString());
					doc.closeListItem();
				}
			}
		}
		if (hasLink) {
			doc.closeList();
		}
		doc.openParagraph(null);
		String[] t = annotation.getComment().split("\n");
		for (int i = 0; i < t.length-1; i++) {
			doc.writeTextln(t[i]);
		}
		doc.writeText(t[t.length-1]);
		doc.closeParagraph();
	}

	/**
	 * import the javascript (DocumentExtra) from js file. 
	 * The javascript is use to allow the user to collapse/expand the stables states in the table.
	 * @throws IOException 
	 * 
	 */
	private void setJavascript() throws IOException {
		StringBuffer javascript = doc.getDocumentExtra("javascript");
		InputStream stream = IOUtils.getStreamForPath(getClass().getPackage(), "makeStableStatesClickable.js");
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String s;
		while ((s = in.readLine()) != null) {
			javascript.append(s);
			javascript.append("\n");
		}
	}
	/**
	 * Set the style for the document.
	 */
	private void setDocumentStyles() {
		DocumentStyle styles = doc.getStyles();
		styles.addStyle("L1");
		styles.addProperty(DocumentStyle.LIST_TYPE, "U");	
		styles.addStyle("table");
		styles.addProperty(DocumentStyle.TABLE_BORDER, new Integer(1));	
	}

	/**
	 * Set the properties (meta-information) for the document.
	 */
	private void setDocumentProperties() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//for dc:date
		doc.setDocumentProperties(new String[] {
				//DocumentWriter.META_AUTHOR,
				DocumentWriter.META_DATE, simpleDateFormat.format(new Date()).toString(),
				//DocumentWriter.META_DESCRIPTION, 
				DocumentWriter.META_GENERATOR, "GINsim",
				//DocumentWriter.META_KEYWORDS,
				DocumentWriter.META_TITLE, graph.getGraphName()
		});
	}
}