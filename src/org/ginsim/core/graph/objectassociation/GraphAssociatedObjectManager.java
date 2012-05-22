package org.ginsim.core.graph.objectassociation;

import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.common.Graph;

/**
 * Implement this interface to save/open objects associated to graph automatically.
 */
public interface GraphAssociatedObjectManager {

    /**
     * @return the name of the object (for the zip entry)
     */
    String getObjectName();
    /**
     * Get the aliases under which the object used to be saved (zip name in old files)
     * 
     * @return the aliases or null
     */
    String[] getAliases();
    /**
     * @param graph
     * @return true if this graph has a relevant associated object
     */
    boolean needSaving( Graph graph);
    /**
     * save the object associated with this graph.
     * 
     * @param out
     * @param graph
     * @throws GsException 
     */
    void doSave(OutputStreamWriter out, Graph graph) throws GsException;
    /**
     * open associated object.
     * 
     * @param is
     * @param graph
     * @throws GsException 
     */
    Object doOpen(InputStream is, Graph graph) throws GsException;
    /**
     * create the associated object for a graph
     * @param graph
     */
	Object doCreate( Graph graph);
	
	/**
	 * get the existing associated object for a graph
	 * 
	 * @param graph
	 * @return
	 */
	Object getObject( Graph graph);
}
