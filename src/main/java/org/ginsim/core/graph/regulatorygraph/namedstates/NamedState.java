package org.ginsim.core.graph.regulatorygraph.namedstates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.colomoto.biolqm.NodeInfo;
import org.ginsim.common.xml.XMLWriter;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.utils.data.NamedObject;

/**
 * Defines a (set of) named state(s): a name associated to an activity pattern.
 *
 * @author Aurelien Naldi
 */
public class NamedState implements NamedObject {
	String name;
	Map<NodeInfo, List<Integer>> m = new HashMap<NodeInfo, List<Integer>>();
	
	public String toString(){
		return name+m.toString();
	}
	
    public void setState(int[] state, List<RegulatoryNode> nodeOrder) {
        setState(state, nodeOrder, false);
    }
    public void setState(int[] state, List<RegulatoryNode> nodeOrder, boolean input) {
        String[] t_s = new String[state.length];
        for (int i=0 ; i<t_s.length ; i++) {
            RegulatoryNode vertex = (RegulatoryNode)nodeOrder.get(i);
            if (vertex.isInput() == input) {
                t_s[i] = vertex + ";" + state[i];
            } else {
                t_s[i] = "";
            }
        }
        setData(t_s, nodeOrder);
    }
    public void setStateAll(int[] state, List<RegulatoryNode> nodeOrder) {
        String[] t_s = new String[state.length];
        for (int i=0 ; i<t_s.length ; i++) {
            RegulatoryNode vertex = (RegulatoryNode)nodeOrder.get(i);
            t_s[i] = vertex + ";" + state[i];
        }
        setData(t_s, nodeOrder);
    }
    
    public Map<NodeInfo, List<Integer>> getMaxValueTable() {
    	
		return m;
	}
    
    public void setMaxValueTable( Map<NodeInfo, List<Integer>> m) {
    	
		this.m = m;
	}
    
	public void setData(String[] t_s, List<RegulatoryNode> nodeOrder) {
        for (int i=0 ; i<t_s.length ; i++) {
            RegulatoryNode vertex = null;
            String[] t_val = t_s[i].split(";");
            // TODO: cleaner fixed mapping
            t_val[0] = XMLWriter.deriveValidId(t_val[0]);
            if (t_val.length > 1) {
                for (int j=0 ; j<nodeOrder.size() ; j++) {
                    if (nodeOrder.get(j).getId().equals(t_val[0])) {
                        vertex = nodeOrder.get(j);
                        break;
                    }
                }
                if (vertex != null) {
                	List<Integer> v_val = new ArrayList<Integer>();
                    for (int j=1 ; j<t_val.length ; j++) {
                        try {
                        	int v = Integer.parseInt(t_val[j]);
                            if (v >= 0 && v <= vertex.getMaxValue()) {
                                boolean ok = true;
                                for (int k=0 ; k<v_val.size() ; k++) {
                                    if (v_val.get(k).equals(v)) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (ok) {
                                    v_val.add(v);
                                }
                            } else {
                                // TODO: report error in file
                            }
                        } catch (NumberFormatException e) {
                            // TODO: report error in file
                        }
                    }
                    if (!v_val.isEmpty() && v_val.size() <= vertex.getMaxValue()) {
                        m.put(vertex.getNodeInfo(), v_val);
                    }
                }
            }
        }
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<NodeInfo,List<Integer>> getMap() {
		return m;
	}

    public byte getFirstValue(NodeInfo ni) {
        List<Integer> values = m.get(ni);
        if (values == null || values.size()< 1) {
            return 0;
        }
        return values.get(0).byteValue();
    }
}
