package fr.univmrs.ibdm.GINsim.export.regulatoryGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;

import fr.univmrs.ibdm.GINsim.data.GsDirectedEdge;
import fr.univmrs.ibdm.GINsim.export.GsAbstractExport;
import fr.univmrs.ibdm.GINsim.export.GsExportConfig;
import fr.univmrs.ibdm.GINsim.global.GsEnv;
import fr.univmrs.ibdm.GINsim.global.GsException;
import fr.univmrs.ibdm.GINsim.graph.GsGraph;
import fr.univmrs.ibdm.GINsim.gui.GsPluggableActionDescriptor;
import fr.univmrs.ibdm.GINsim.gui.GsStackDialog;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryVertex;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.OmddNode;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.initialState.GsInitialState;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.mutant.GsRegulatoryMutantDef;

/**
 * Encode a graph to SMV format.
 */
public class GsSMVExport extends GsAbstractExport {
    static transient Hashtable hash;

    public GsSMVExport() {
		id = "SMV";
		extension = ".smv";
		filter = new String[] { "smv" };
		filterDescr = "SMV files";
    }
    
	protected void doExport(GsExportConfig config) {
		encode((GsRegulatoryGraph)config.getGraph(), 
				config.getFilename(), 
				(GsSMVexportConfig)config.getSpecificConfig());
	}

	public GsPluggableActionDescriptor[] getT_action(int actionType, GsGraph graph) {
        if (graph instanceof GsRegulatoryGraph) {
        	return new GsPluggableActionDescriptor[] {
        			new GsPluggableActionDescriptor("STR_modelChecker", "STR_modelCheckerExport_descr", null, this, ACTION_EXPORT, 0)
        	};
        }
        return null;
	}
	
	public boolean needConfig(GsExportConfig config) {
		return true;
	}

	protected JComponent getConfigPanel(GsExportConfig config, GsStackDialog dialog) {
        return new GsSMVExportConfigPanel(config, dialog, true, false);
	}
	
	private static String getInitState(Object vertex, Map m) {
    	Vector v = (Vector)m.get(vertex);
    	if (v != null && v.size() > 0) {
	    	String s = ""+v.get(0);
	    	if (v.size() > 1) {
	    		s = "{"+s;
	    		for (int j=1 ; j<v.size() ; j++) {
	    			s += ","+v.get(j);
	    		}
	    		s += "}";
	    	}
	    	return s;
    	}
		return null;
	}
	
    /**
     * @param graph
     * @param selectedOnly
     * @param fileName
     * @param config store the configuration
     */
    public static void encode(GsRegulatoryGraph graph, String fileName, GsSMVexportConfig config) {
        hash = new Hashtable();
        DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG);
        String date = dateformat.format(new Date());
        try {
            FileWriter out = new FileWriter(fileName);
            boolean sync = config.isSync();
            Iterator it = config.getInitialState().keySet().iterator();
            Map m_initstates;
            if (it.hasNext()) {
            	m_initstates = ((GsInitialState)it.next()).getMap();
            } else {
            	m_initstates = new HashMap();
            }
            if (m_initstates == null) {
            	m_initstates = new HashMap();
            }
            
            GsRegulatoryMutantDef mutant = config.getMutant();
            out.write("-- SMV file generated by GINsim - " + date + "\n\n");
            
            Vector nodeOrder = graph.getNodeOrder();
            String[] t_regulators = new String[nodeOrder.size()];
            int[] t_cst = new int[nodeOrder.size()];
            GsRegulatoryVertex[] t_vertex = new GsRegulatoryVertex[nodeOrder.size()];
            OmddNode[] t_tree = graph.getAllTrees(true);
            if (mutant != null) {
                mutant.apply(t_tree, nodeOrder, false);
            }
            if (sync) {
                out.write("\nMODULE main\n");
                out.write("  VAR\n");
                for (int i = 0; i < t_vertex.length; i++) {
                    GsRegulatoryVertex vertex = (GsRegulatoryVertex) nodeOrder.get(i);
                    t_vertex[i] = vertex;
                    t_regulators[i] = vertex.getId();
                    String s_levels = "0";
                    for (int j = 1; j <= vertex.getMaxValue(); j++) {
                        s_levels += ", " + j;
                    }
                    out.write("  "+vertex.getId()+" : {" + s_levels + "};\n");
                    // if not in the boolean case, add a temporary variable to (in/de)crease smoothly
                    if (vertex.getMaxValue() > 1) {
                        out.write("  "+vertex.getId()+"_nlevel : {" + s_levels + "};\n");
                    }
                }
                out.write("  ASSIGN\n");
                for (int i = 0; i < nodeOrder.size(); i++) {
                	String s_init = getInitState(nodeOrder.get(i), m_initstates);
                    if (s_init == null) {
                        out.write("--    init("+t_regulators[i]+") := 0;\n");
                    } else {
                        out.write("      init("+t_regulators[i]+") := "+s_init+";\n");
                    }
                }
                for (int i = 0; i < nodeOrder.size(); i++) {
                    GsRegulatoryVertex vertex = t_vertex[i];
                    if (vertex.getMaxValue() > 1) {
                        out.write("  "+vertex.getId()+"_nlevel := \n");
                    } else {
                        out.write("  next("+vertex.getId()+") := \n");
                    }
                    out.write("      case\n");
                    for (int j = 0; j < t_cst.length; j++) {
                        t_cst[j] = -1;
                    }
                    node2SMV( t_tree[i], out, t_vertex,
                            t_cst, i, sync);
                    out.write("      esac;\n");
    
                    if (vertex.getMaxValue() > 1) {
                        out.write("    next("+vertex.getId()+") := \n");
                        out.write("      case\n");
                        out.write("        ("+vertex.getId()+"_nlevel > "+vertex.getId()+") : "+vertex.getId()+" + 1;\n");
                        out.write("        ("+vertex.getId()+"_nlevel < "+vertex.getId()+") : "+vertex.getId()+" - 1;\n");
                        out.write("        ("+vertex.getId()+"_nlevel = "+vertex.getId()+") : "+vertex.getId()+";\n");
                        out.write("      esac;\n");
                    }
                }
            } else { // asynchronous
                for (int i = 0; i < t_vertex.length; i++) {
                    GsRegulatoryVertex vertex = (GsRegulatoryVertex) nodeOrder.get(i);
                    t_vertex[i] = vertex;
                    t_regulators[i] = vertex.getId();
                }

                for (int i = 0; i < nodeOrder.size(); i++) {
                    GsRegulatoryVertex vertex = t_vertex[i];
                    // get regulators
                    List l_regulators = graph.getGraphManager().getIncomingEdges(vertex);
                    String s_regulators = "";
                    if (l_regulators.size() > 0) {
                        for (int j = 0; j < l_regulators.size(); j++) {
                            GsRegulatoryVertex regulator = (GsRegulatoryVertex)((GsDirectedEdge) l_regulators.get(j)).getSourceVertex();
                            if (regulator != vertex) {
                                s_regulators +=  regulator+ ", ";
                            }
                        }
                        // always add a self-control (for mutants...)
                        s_regulators += ""+vertex;
                    }
                    t_regulators[i] = "    " + vertex.getId() + "\t: process _"
                                + vertex.getId() + "(" + s_regulators + ");\n";
                    
                    out.write("MODULE _" + vertex.getId() + "(" + s_regulators
                            + ")\n");
                    String s_levels = "0";
                    for (int j = 1; j <= vertex.getMaxValue(); j++) {
                        s_levels += ", " + j;
                    }
    
                    out.write("  VAR\n    level : {" + s_levels + "};\n");
                    // if not in the boolean case, add a temporary variable to (in/de)crease smoothly
                    if (vertex.getMaxValue() > 1) {
                        out.write("    nlevel : {" + s_levels + "};\n");
                    }
                    out.write("  ASSIGN\n");
                	String s_init = getInitState(nodeOrder.get(i), m_initstates);
                	if (s_init == null) {
                		out.write("--    init(level) := 0;\n");
                	} else {
                		out.write("    init(level) := "+s_init+";\n");
                	}
                    if (vertex.getMaxValue() > 1) {
                        out.write("    nlevel := \n");
                    } else {
                        out.write("    next(level) := \n");
                    }
                    out.write("      case\n");
                    for (int j = 0; j < t_cst.length; j++) {
                        t_cst[j] = -1;
                    }
                    node2SMV(t_tree[i], out, t_vertex,
                            t_cst, i, sync);
                    out.write("      esac;\n");
    
                    if (vertex.getMaxValue() > 1) {
                        out.write("    next(level) := \n");
                        out.write("      case\n");
                        out.write("        (nlevel > level) : level + 1;\n");
                        out.write("        (nlevel < level) : level - 1;\n");
                        out.write("        (nlevel = level) : level;\n");
                        out.write("      esac;\n");
                    }
                    out.write("FAIRNESS running\n\n");
                }
    
                out.write("\nMODULE main\n");
                out.write("  VAR\n");
                for (int i = 0; i < t_regulators.length; i++) {
                    out.write(t_regulators[i]);
                }
    
                // ask for one stable state
                if (true) {
                    out.write("\n--SPEC !EF (");
                    for (int i = 0; i < t_vertex.length; i++) {
                        out.write(" (");
                        GsRegulatoryVertex vertex = t_vertex[i];
                        for (int j = 0; j <= vertex.getMaxValue(); j++) {
                            if (j != 0) {
                                out.write("  ");
                            }
                            out.write("(" + vertex + ".level = " + j + " &  AX "
                                    + vertex + ".level = " + j + ")");
                            if (j < vertex.getMaxValue()) {
                                out.write(" |");
                            }
                        }
                        out.write(")");
                        if (i < t_vertex.length - 1) {
                            out.write(" &");
                        }
                    }
                    out.write(")\n");
                }
            }
            // add the test
            if (config.thetest != null) {
            	out.write("\n");
            	out.write(config.thetest+"\n");
            }
           
            // Close main tags
            out.close();
        } catch (IOException e) {
            GsEnv.error(new GsException(GsException.GRAVITY_ERROR, e
                    .getLocalizedMessage()), null);
        }
    }

    static private void node2SMV(OmddNode node, FileWriter out,
            GsRegulatoryVertex[] t_names, int[] t_cst, int index, boolean sync)
            throws IOException {
        if (node.next == null) { // this is a leaf, write the constraint
            String s = "";
            String s_assign = (sync ? " = " : ".level = ");
            for (int i = 0; i < t_cst.length; i++) {
                if (t_cst[i] != -1) {
                	if (sync || i != index) {
                		s += "(" + t_names[i] + s_assign + t_cst[i] + ") & ";
                	} else {
                		s += "(level = " + t_cst[i] + ") & ";
                	}
                }
            }
            if ("".equals(s)) {
                s = "1 ";
            } else {
                s = s.substring(0, s.length() - 2);
            }
            // FIXME: replace node.value with smart incremental move
            out.write("        " + s + " : " + node.value + ";\n");
            return;
        }
        for (int i = 0; i < node.next.length; i++) {
            t_cst[node.level] = i;
            node2SMV(node.next[i], out, t_names, t_cst, index, sync);
        }
        t_cst[node.level] = -1;
    }
}
