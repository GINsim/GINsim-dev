package fr.univmrs.ibdm.GINsim.regulatoryGraph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import fr.univmrs.ibdm.GINsim.global.GsOptions;
import fr.univmrs.ibdm.GINsim.graph.GsGraphOptionPanel;
import fr.univmrs.ibdm.GINsim.manageressources.Translator;

/**
 * offer save option for regulatory graphs 
 */
public class GsRegulatoryGraphOptionPanel extends JPanel implements
		GsGraphOptionPanel {

	private static final long serialVersionUID = 4585614812066176148L;
	private JComboBox comboBox;
    private JCheckBox extended;
    private JCheckBox compressed;
    
    // FIXME: make uncompresed save just work
    private static final boolean showCompressed = false;
	
	/**
	 * create the save option panel for ginml based graphs
	 * @param t_mode allowed save mode
     * @param mode selected save mode
	 */
	public GsRegulatoryGraphOptionPanel (Object[] t_mode, int mode) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        if (t_mode != null) {
    		this.add(getComboBox(t_mode, mode), c);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
        }
        this.add(getExtended(), c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0,10,0,0);
        if (showCompressed) {
        	this.add(getCompressed(), c);
        }
        comboBox.setSelectedIndex(mode);
	}
	
	/**
	 * @see fr.univmrs.ibdm.GINsim.graph.GsGraphOptionPanel#getSaveMode()
	 */
	public int getSaveMode() {
        if (comboBox != null) {
            return comboBox.getSelectedIndex();
        }
		return 0;
	}
    
    public boolean isExtended() {
        GsOptions.setOption("extendedSave", extended.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        return extended.isSelected();
    }

    public boolean isCompressed() {
    	if (!showCompressed) {
    		return true;
    	}
        GsOptions.setOption("compressedSave", compressed.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        return compressed.isSelected();
    }

    private JCheckBox getExtended() {
        if (extended == null) {
            extended = new JCheckBox(Translator.getString("STR_extendedSave"));
            extended.setSelected(GsOptions.getOption("extendedSave", Boolean.FALSE).equals(Boolean.TRUE));
        }
        return extended;
    }
	
    private JCheckBox getCompressed() {
        if (compressed == null) {
        	compressed = new JCheckBox(Translator.getString("STR_compressedSave"));
        	compressed.setSelected(GsOptions.getOption("compressedSave", Boolean.FALSE).equals(Boolean.TRUE));
        }
        return compressed;
    }
	
	private JComboBox getComboBox(Object[] t, int mode) {
		if (comboBox == null) {
			comboBox = new JComboBox();
            for (int i=0 ; i<t.length ; i++) {
                comboBox.addItem(t[i]);
            }
			comboBox.setSelectedIndex(mode);
		}
		return comboBox;
	}

    public String getExtension() {
        if (extended.isSelected()) {
            return ".zginml";
        }
        return ".ginml";
    }
}
