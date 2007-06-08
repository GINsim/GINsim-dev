package fr.univmrs.ibdm.GINsim.xml;

import java.io.IOException;

/**
 * object that can save themself to XML must implement this interface to let others know.
 */
public interface GsXMLize {
    
    /**
     * write the XML representation of this object to a file
     * 
     * @param out
     * @param param
     * @param mode
     * @throws IOException
     */
	public void toXML(GsXMLWriter out, Object param, int mode) throws IOException;	
}
