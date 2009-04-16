package fr.univmrs.tagc.GINsim.regulatoryGraph;

import java.util.*;

public class LogicalParameterList extends ArrayList {
	private HashSet manualp, functionp;
	boolean updateDup = true;
	int nbDup = 0;

	public Object remove(int index) {
		int i;
		Iterator it;
		GsLogicalParameter lp = (GsLogicalParameter)get(index), lp2;
		if (manualp.contains(lp)) {
			manualp.remove(lp);
			lp.isDup = lp.hasConflict = false;
			if (!functionp.contains(lp))
				super.remove(index);
			else {
				i = indexOf(lp);
				remove(lp);
				it = functionp.iterator();
				while (it.hasNext()) {
					lp2 = (GsLogicalParameter)it.next();
					if (lp.equals(lp2)) {
						add(i, lp2);
						break;
					}
				}
			}
			refreshDupAndConflicts();
		}
		return lp;
	}

	public LogicalParameterList() {
		super();
		manualp = new HashSet();
		functionp = new HashSet();
	}
	public boolean isManual(int i) {
		if (i < size()) {
			Object o = get(i);
			return manualp.contains(o);
		}
		return false;
	}
	public boolean isFunction(int i) {
		if (i < size()) {
			Object o = get(i);
			return functionp.contains(o);
		}
		return false;
	}
	public boolean addLogicalParameter(GsLogicalParameter newParam, boolean manual) {
		boolean r = false;
		int i;
		if (!contains(newParam)) {
			r = add(newParam);
			if (manual)
				manualp.add(newParam);
			else
				functionp.add(newParam);
			findDup(newParam, manual ? functionp : manualp);
		}
		else if (manualp.contains(newParam) && !manual) {
			i = indexOf(newParam);
			newParam = (GsLogicalParameter)get(i);
			functionp.add(newParam);
			findDup(newParam, manual ? functionp : manualp);
		}
		else if (functionp.contains(newParam) && manual) {
			i = indexOf(newParam);
			newParam = (GsLogicalParameter)get(i);
			manualp.add(newParam);
			findDup(newParam, manual ? functionp : manualp);
		}
		return r;
  }
	public void cleanupDup() {
		Iterator it = functionp.iterator(), it2;
		while (it.hasNext()) {
			GsLogicalParameter param = (GsLogicalParameter)it.next();
			param.isDup = false;
			param.hasConflict = false;
		}
		it = manualp.iterator();
		Vector toremove = new Vector();
		while (it.hasNext()) {
			GsLogicalParameter param = (GsLogicalParameter)it.next();
			param.isDup = false;
			param.hasConflict = false;
			it2 = functionp.iterator();
			while (it2.hasNext()) {
				GsLogicalParameter other = (GsLogicalParameter) it2.next();
				if (param.equals(other)) {
					if (other.getValue() == param.getValue()) {
						toremove.addElement(param);
					}
					else {
						param.hasConflict = true;
						other.hasConflict = true;
					}
					break;
				}
			}
		}
		manualp.removeAll(toremove);
		updateDup = true;
	}
	private void setParameters(List logicalParameters, boolean manual) {
		HashSet hs1 = manual ? manualp : functionp;
		HashSet hs2 = manual ? functionp : manualp;
		Iterator it = logicalParameters.iterator();
		GsLogicalParameter o;
		int i;
		HashSet hs;
		while (it.hasNext()) {
			o = (GsLogicalParameter)it.next();
			if (!contains(o)) {
				hs1.add(o);
				add(o);
			}
			else if (hs2.contains(o) && !hs1.contains(o))
				hs1.add(o);
			else if (hs1.contains(o) && !hs2.contains(o)) {
				i = indexOf(o);
				if (o.getValue() != ((GsLogicalParameter)get(i)).getValue()) {
					remove(o);
					add(i, o);
					hs1.remove(o);
					hs1.add(o);
				}
			}
			else if (hs1.contains(o) && hs2.contains(o)) {
				hs1.remove(o);
				hs1.add(o);
			}
		}
		it = hs1.iterator();
		hs = new HashSet();
		while (it.hasNext()) {
			o = (GsLogicalParameter)it.next();
			if (!logicalParameters.contains(o))
				if (!hs2.contains(o)) {
					hs.add(o);
					remove(o);
				}
				else
					hs.add(o);
		}
		hs1.removeAll(hs);
		refreshDupAndConflicts();
	}
	public void setFunctionParameters(List logicalParameters) {
		setParameters(logicalParameters, false);
	}
	public void setManualParameters(List logicalParameters) {
		setParameters(logicalParameters, true);
	}
	private void refreshDupAndConflicts() {
		if (!updateDup) return;
		nbDup = 0;
		Iterator it = manualp.iterator();
		GsLogicalParameter param;
		while (it.hasNext()) {
			param = (GsLogicalParameter)it.next();
			param.isDup = false;
			param.hasConflict = false;
		}
		it = functionp.iterator();
		while (it.hasNext()) {
			param = (GsLogicalParameter)it.next();
			findDup(param, manualp);
		}
	}
	public Iterator iterator(boolean manual) {
		return manualp.iterator();
	}
	public void updateInteraction(int index, Vector edges) {
		Object o = get(index);
		if (functionp.contains(o)) {
			return;
		}
		GsLogicalParameter I = (GsLogicalParameter)o;
		List oldList = I.getEdges();
		I.setEdges(edges);
		for (int i=0 ; i<size() ; i++) {
			if ( i!= index && get(i).equals(I)) {
				I.setEdges(oldList);
				return;
			}
		}
		refreshDupAndConflicts();
	}
	private void findDup(GsLogicalParameter param, HashSet l) {
		if (!updateDup) return;
		param.isDup = false;
		param.hasConflict = false;
		Iterator it = l.iterator();
		while (it.hasNext()) {
			GsLogicalParameter p = (GsLogicalParameter)it.next();
			if (p.equals(param)) {
				nbDup++;
				if (p.getValue() == param.getValue()) {
					p.isDup = param.isDup = true;
				} else {
					p.hasConflict = param.hasConflict = true;
				}
				break;
			}
		}
	}
	public int getRealSize() {
		return size();
	}
	public int getManualSize() {
		return manualp.size();
	}
	public void cleanupInteraction() {
		GsLogicalParameter lp;
		for (int i=size()-1 ; i>=0 ; i--) {
			lp = (GsLogicalParameter)get(i);
			if (lp.isDurty()) {
				manualp.remove(lp);
				functionp.remove(lp);
				remove(lp);
			}
		}
		refreshDupAndConflicts();
	}
	public void applyNewMaxValue(short max, GsRegulatoryGraph graph, List l) {
		Iterator it = manualp.iterator();
		while (it.hasNext()) {
			GsLogicalParameter param = (GsLogicalParameter)it.next();
			if (param.getValue() > max) {
				l.add(param);
			}
		}
	}
	public void setParameterValue(int rowIndex, int value, GsRegulatoryGraph graph) {
		GsLogicalParameter param = (GsLogicalParameter)get(rowIndex);
		param.setValue(value, graph);
		if (param.hasConflict || param.isDup) {
			findDup(param, functionp);
		}
	}
	public void setUpdateDup(boolean updateDup) {
		this.updateDup = updateDup;
		refreshDupAndConflicts();
	}
	public boolean moveElement(int index, int to) {
		if (index < 0 || to < 0) {
			return false;
		}
		Object obj=super.remove(index);
		add(to, obj);

		return true;
	}
	
	/**
	 * Apply all the logical parameters from this list to the clone vertex (from the new graph)
	 * @param clone the vertex from the new graph
	 * @param copyMap a map of multi-edge from the old graph to the new one.
	 */
	public void applyNewGraph(GsRegulatoryVertex clone, Map copyMap) {
		for (Iterator it = iterator(); it.hasNext();) {
			GsLogicalParameter param = (GsLogicalParameter)it.next();
			param.applyNewGraph(clone, copyMap);
		}
	}
}
