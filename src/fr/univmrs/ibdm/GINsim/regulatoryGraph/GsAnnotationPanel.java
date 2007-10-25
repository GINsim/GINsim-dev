package fr.univmrs.ibdm.GINsim.regulatoryGraph;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import fr.univmrs.ibdm.GINsim.data.GsAnnotation;
import fr.univmrs.ibdm.GINsim.global.Tools;
import fr.univmrs.ibdm.GINsim.gui.GsParameterPanel;
import fr.univmrs.tagc.datastore.GenericPropertyInfo;
import fr.univmrs.tagc.datastore.ObjectPropertyEditorUI;
import fr.univmrs.tagc.datastore.SimpleGenericList;
import fr.univmrs.tagc.datastore.gui.GenericListPanel;
import fr.univmrs.tagc.datastore.gui.GenericPropertyHolder;
/**
 * Panel to edit annotations
 */
public class GsAnnotationPanel extends GsParameterPanel 
	implements ObjectPropertyEditorUI, TableModelListener {

	private static final long serialVersionUID = -8542547209276966234L;

	private GsAnnotation currentNote = null;
    private boolean listenChanges = true;
	SimpleGenericList linkList;
	GenericListPanel linkListPanel = null;
	private JScrollPane jScrollPane = null;
	private JTextArea jTextArea = null;

	private GenericPropertyInfo	pinfo;
	/**
	 * This method initializes 
	 * 
	 */
	public GsAnnotationPanel() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.setSize(542, 60);
        gridBagConstraints15.gridx = 0;
        gridBagConstraints15.gridy = 0;
        gridBagConstraints15.weightx = 1.0;
        gridBagConstraints15.weighty = 1.0;
        gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
        this.add(getLinkList(), gridBagConstraints15);
        this.setPreferredSize(new java.awt.Dimension(800,60));
        gridBagConstraints16.weightx = 1.0;
        gridBagConstraints16.weighty = 1.0;
        gridBagConstraints16.fill = java.awt.GridBagConstraints.BOTH;
        this.add(getJScrollPane(), gridBagConstraints16);
	}
    public void setEditedObject(Object obj) {
        if (obj != null && obj instanceof GsAnnotation) {
            listenChanges = false;
            currentNote = (GsAnnotation)obj;
            linkList.setData(currentNote.getLinkList());
            jTextArea.setText(currentNote.getComment());
            listenChanges = true;
        }
    }
    public Component getLinkList() {
    	if (linkListPanel == null) {
    		linkList = new LinkList();
    		linkListPanel = new GenericListPanel();
    		linkListPanel.setList(linkList);
    	}
    	return linkListPanel;
    }
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
            jTextArea.setLineWrap(true);
            jTextArea.setWrapStyleWord(true);
			jTextArea.addFocusListener(new java.awt.event.FocusAdapter() { 
				public void focusLost(java.awt.event.FocusEvent e) {
				    applyComment();
				}
			});
		}
		return jTextArea;
	}
    protected void applyComment() {
    	if (currentNote == null) {
    		return;
    	}
        if (!currentNote.getComment().equals(jTextArea.getText())) {
        		currentNote.setComment(jTextArea.getText());
            if (listenChanges && graph != null) {
                graph.fireMetaChange();
            }
        }
    }
    
    public void tableChanged(TableModelEvent e) {
        if (listenChanges && graph != null) {
            graph.fireMetaChange();
        }
    }
	public void apply() {
	}
	public void refresh(boolean force) {
		setEditedObject(pinfo.getRawValue());
	}
	public void setEditedProperty(GenericPropertyInfo pinfo,
			GenericPropertyHolder panel) {
		this.pinfo = pinfo;
		panel.addField(this, pinfo, 0);
	}
}

class LinkList extends SimpleGenericList {
	LinkList() {
		canAdd = true;
		hasAction = true;
		canRemove = true;
		canEdit = true;
		inlineAddDel = true;
	}
	public Object doCreate(String name, int type) {
		return name;
	}
	public void doRun(int index) {
		Tools.webBrowse((String)v_data.get(index));
	}
}