package fr.univmrs.ibdm.GINsim.regulatoryGraph.models;

import javax.swing.JList;
import javax.swing.JSpinner;

import fr.univmrs.ibdm.GINsim.global.Tools;
import fr.univmrs.ibdm.GINsim.graph.GsGraph;
import fr.univmrs.ibdm.GINsim.manageressources.Translator;
import fr.univmrs.ibdm.GINsim.regulatoryGraph.GsRegulatoryMultiEdge;
import fr.univmrs.tagc.datastore.models.MaxSpinModel;
import fr.univmrs.tagc.datastore.models.MinMaxSpinModel;
import fr.univmrs.tagc.datastore.models.MinSpinModel;

/**
 * model controlling the behavior of min and max spinbuttons for an edge
 */
public class GsEdgeMinMaxSpinModel implements MinMaxSpinModel {
    
    private int index = 0;
    private GsRegulatoryMultiEdge medge = null;
    private MinSpinModel m_min;
    private MaxSpinModel m_max;
    private JList jlist = null;
    private GsGraph graph = null;
    private boolean update = true;

    // FIXME: this class should die
    
    /**
     * @param graph
     * @param jlist
     */
    public GsEdgeMinMaxSpinModel(GsGraph graph, JList jlist) {
        super();
        this.graph = graph;
        this.jlist = jlist;
        m_min = new MinSpinModel(this);
        m_max = new MaxSpinModel(this);
    }

    public Object getNextMaxValue() {
            return getMaxValue();
    }

    public Object getPreviousMaxValue() {
            return getMaxValue();
    }

    public Object getMaxValue() {
        if (medge == null || medge == null) {
            return Tools.IZ;
        }
        if (medge.getMax(index) == -1) {
            return "Max";
        }
        return new Integer(medge.getMax(index));
    }

    public void setMaxValue(Object value) {
            return;
    }
    public Object getNextMinValue() {
        if (!graph.isEditAllowed() || medge == null) {
            return getMinValue();
        }
        short cur = medge.getMin(index);
        short max = medge.getSource().getMaxValue();
        if (update && cur < max) {
            graph.fireMetaChange();
        }
        
        if (cur < max) {
            medge.setMin(index, (short)(cur+1));
            m_max.update();
        }
        m_min.update();
        ((GsDirectedEdgeListModel)jlist.getModel()).update();
       return new Integer(medge.getMin(index));
    }

    public Object getPreviousMinValue() {
        if (!graph.isEditAllowed() || medge == null) {
            return getMinValue();
        }
        short cur = medge.getMin(index);
        if (update && cur > 1) {
            graph.fireMetaChange();
        }
        
        if (cur > 1) {
            medge.setMin(index, (short)(cur-1));
        }
        m_min.update();
        ((GsDirectedEdgeListModel)jlist.getModel()).update();
        return new Integer(medge.getMin(index));
    }

    public Object getMinValue() {
        if (medge == null || medge == null) {
            return Tools.IZ;
        }
        return new Integer(medge.getMin(index));
    }

    public void setMinValue(Object value) {
        if (!graph.isEditAllowed()) {
            return;
        }
        if (value instanceof String) {
            try {
                medge.setMin(index, (short)Integer.parseInt(value.toString()));
                if (update) {
                    graph.fireMetaChange();
                }
            } catch (NumberFormatException e) {}

        } else if (value instanceof Integer ){
            medge.setMin(index, ((Integer)value).shortValue());
            m_min.update();
            m_max.update();
            ((GsDirectedEdgeListModel)jlist.getModel()).update();
        }
    }

    /**
     * change the edited edge.
     * @param index
     */
    public void setEdge(int index) {
        update = false;
        this.index = index;
        m_max.update();
        m_min.update();
        update = true;
    }
    /**
     * change the edited multiedge.
     * @param medge
     */
    public void setMedge(GsRegulatoryMultiEdge medge) {
        this.medge = medge;
    }
    
    public JSpinner getSMin() {
        JSpinner smin = new JSpinner(m_min);
        smin.setEditor(m_min.getEditor());
        return smin;
    }
    public JSpinner getSMax() {
        JSpinner smax = new JSpinner(m_max);
        smax.setEditor(m_max.getEditor());
        return smax;
    }

	public String getMaxName() {
		return Translator.getString("STR_max");
	}

	public String getMinName() {
		return Translator.getString("STR_min");
	}

	public void setEditedObject(Object rawValue) {
	}
}
