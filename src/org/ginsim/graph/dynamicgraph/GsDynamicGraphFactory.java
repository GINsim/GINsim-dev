package org.ginsim.graph.dynamicgraph;

import java.util.List;

import org.ginsim.graph.common.GraphFactory;
import org.mangosdk.spi.ProviderFor;

import fr.univmrs.tagc.common.Debugger;

/**
 * descriptor for dynamic (state transition) graphs.
 */
@ProviderFor( GraphFactory.class)
public class GsDynamicGraphFactory implements GraphFactory<GsDynamicGraph> {

    private static GsDynamicGraphFactory instance = null;
	
    public GsDynamicGraphFactory(){
    	
    	if( instance == null){
    		instance = this;
    	}
    }
    
    /**
     * @return an instance of this graphDescriptor.
     */
    public static GsDynamicGraphFactory getInstance() {
    	
        if (instance == null) {
            instance = new GsDynamicGraphFactory();
        }
        return instance;
    }

    @Override
	public Class<GsDynamicGraph> getGraphClass(){
		
		return GsDynamicGraph.class;
	}
	
    @Override
	public String getGraphType() {
		
		return "dynamic";
	}
	
    @Override
	public Class getParser(){
		
		return GsDynamicParser.class;
	}
	
    @Override
	public GsDynamicGraph create(){
		
		return new DynamicGraphImpl();
	}
	
	
    @Override
	public GsDynamicGraph create( Object param){
		
    	if (param instanceof Boolean) {
    		return new DynamicGraphImpl( (Boolean)param);
    	}
		
    	if (param instanceof List) {
    		return new DynamicGraphImpl( (List)param);
    	}
    	
    	Debugger.log("Unsupported parameter type when creating a STG: " + param);
    	return create();
	}

}
