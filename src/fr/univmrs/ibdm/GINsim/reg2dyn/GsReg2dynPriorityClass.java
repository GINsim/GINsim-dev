package fr.univmrs.ibdm.GINsim.reg2dyn;

import fr.univmrs.ibdm.GINsim.global.GsNamedObject;
import fr.univmrs.tagc.datastore.MultiColObject;


/**
 * a priority class for the simulation.
 * each class has a name, a simulation mode and a list of elements
 */
public class GsReg2dynPriorityClass implements MultiColObject, GsNamedObject {
    
    /** this class is synchronous */
    public static final int SYNCHRONOUS = 0;
    /** this class is asynchronous */
    public static final int ASYNCHRONOUS = 1;
    /** rank of this class */
    public int rank;
    
    private String name;
    private int mode;
    
    /**
     * 
     */
    public GsReg2dynPriorityClass() {
        this(0, null);
    }
    /**
     * @param priority
     */
    public GsReg2dynPriorityClass(int priority, String name) {
        this.mode = ASYNCHRONOUS;
        this.rank = priority;
        this.name = name==null ? "new class": name;
    }
    
    public String toString() {
        return rank+": "+name + " ; "+(mode==ASYNCHRONOUS ? "async" : "sync");
    }
    
    /**
     * change the simulation mode for this class
     * 
     * @param mode
     */
    public void setMode(int mode) {
        if (mode == SYNCHRONOUS || mode == ASYNCHRONOUS) {
            this.mode = mode;
        }
    }

    /**
     * @return the simulation mode for this class
     */
    public int getMode() {
        return mode;
    }
    
    /**
     * change this class' name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the name of this priority class
     */
    public String getName() {
        return name;
    }
    
    public Object clone() {
    	GsReg2dynPriorityClass clone = new GsReg2dynPriorityClass();
    	clone.mode = mode;
    	clone.name = name;
    	clone.rank = rank;
    	return clone;
    }
	public Object getVal(int index) {
		switch (index) {
			case 0:
				return ""+rank;
			case 1:
				if (mode == SYNCHRONOUS) {
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			case 2:
				return name;
		}
		return null;
	}
    
	public boolean setVal(int index, Object val) {
		switch (index) {
			case 1:
				if (val == Boolean.TRUE) {
					mode = SYNCHRONOUS;
				} else {
					mode = ASYNCHRONOUS;
				}
				return true;
			case 2:
				name = val.toString();
				return true;
		}
		return false;
	}
    
}
