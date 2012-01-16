package org.ginsim.gui.utils.data;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import org.ginsim.gui.utils.data.models.SpinModel;


public class SpinEditor implements ObjectPropertyEditorUI {

	SpinModel model;
	GenericPropertyInfo pinfo;
	
	@Override
	public void apply() {
	}
	@Override
	public void release() {
	}

	@Override
	public void refresh(boolean force) {
		model.setEditedObject(pinfo.getRawValue());
	}

	@Override
	public void setEditedProperty(GenericPropertyInfo pinfo,
			GenericPropertyHolder panel) {
		this.model = (SpinModel)pinfo.data;
		this.pinfo = pinfo;
		panel.addField(new JLabel(pinfo.name), pinfo, 0);
		panel.addField(new JSpinner(model), pinfo, 1);
	}
}
