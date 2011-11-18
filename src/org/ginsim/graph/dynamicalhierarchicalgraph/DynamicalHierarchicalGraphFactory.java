package org.ginsim.graph.dynamicalhierarchicalgraph;

import java.util.List;

import org.ginsim.graph.common.GraphFactory;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.common.Debugger;

/**
 * descriptor for dynamic hierarchical graphs.
 */
@ProviderFor( GraphFactory.class)
public class DynamicalHierarchicalGraphFactory implements GraphFactory<GsDynamicalHierarchicalGraph> {
	
    private static DynamicalHierarchicalGraphFactory instance = null;
    
    public DynamicalHierarchicalGraphFactory(){
    	
    	if( instance == null){
    		instance = this;
    	}
    }
    
	/**
     * @return an instance of this graphDescriptor.
     */
    public static DynamicalHierarchicalGraphFactory getInstance() {
    	
        if (instance == null) {
            instance = new DynamicalHierarchicalGraphFactory();
        }
        return instance;
    }


    @Override
	public Class<GsDynamicalHierarchicalGraph> getGraphClass(){
		
		return GsDynamicalHierarchicalGraph.class;
	}
	
    @Override
	public String getGraphType() {
		
		return "dynamicalHierarchicalGraph";
	}
	
    @Override
	public Class getParser(){
		
		return GsDynamicalHierarchicalParser.class;
	}
	
    @Override
	public GsDynamicalHierarchicalGraph create(){
		
		return new DynamicalHierarchicalGraphImpl();
	}
	
    @Override
	public GsDynamicalHierarchicalGraph create( Object param){
		
    	if (param instanceof Boolean) {
    		return new DynamicalHierarchicalGraphImpl( (Boolean)param);
    	}
    	
    	if (param instanceof List) {
    		return new DynamicalHierarchicalGraphImpl( (List)param);
    	}
    	
    	Debugger.log("Unsupported parameter type when creating a STG: " + param);
    	return create();
	}
}
