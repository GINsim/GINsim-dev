package org.ginsim.servicegui.tool.reg2dyn;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ginsim.common.exception.GsException;
import org.ginsim.common.utils.GUIMessageUtils;
import org.ginsim.common.utils.Translator;
import org.ginsim.common.utils.log.LogManager;
import org.ginsim.core.graph.common.Graph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.initialstate.InitialStatesIterator;
import org.ginsim.servicegui.tool.reg2dyn.helpers.STGSimulationHelper;
import org.ginsim.servicegui.tool.reg2dyn.helpers.SimulationHelper;



/**
 * This is the main part of the simulation. It supports pluggable backends
 * to generate the following states and to switch from building a full
 * state transition graph to a "simple" reachability set.
 */
public class Simulation extends Thread implements Runnable {

	protected LinkedList queue = new LinkedList(); // exploration queue

	protected SimulationManager frame;
	protected int maxnodes, maxdepth;
	protected Iterator<byte[]> initStatesIterator;
	protected SimulationHelper helper;
	protected SimulationUpdater updater;

	protected boolean breadthFirst = false;
	public int nbnode = 0;
	protected boolean ready = false;
	
	/**
	 * Constructs an empty dynamic graph
	 *
	 * @param regGraph the regulatory graph on which we are working
	 * @param frame
	 * @param params
	 */
    public Simulation(RegulatoryGraph regGraph, SimulationManager frame, SimulationParameters params) {
        this(regGraph, frame, params, true, true);
    }
    public Simulation(RegulatoryGraph regGraph, SimulationManager frame, SimulationParameters params, boolean runNow) {
        this(regGraph, frame, params, runNow, true);
    }
    public Simulation(RegulatoryGraph regGraph, SimulationManager frame, SimulationParameters params, boolean runNow, boolean useInit) {
		this.frame = frame;
		this.maxdepth = params.maxdepth;
		this.maxnodes = params.maxnodes;

		if (params.simulationStrategy == SimulationParameters.STRATEGY_STG) {
			helper = new STGSimulationHelper(regGraph, params);
		}
		breadthFirst = params.breadthFirst;
   		updater = SimulationUpdater.getInstance(regGraph, params);
   		if (useInit) {
   		    initStatesIterator = new InitialStatesIterator(params.nodeOrder, params);
   		}
   		if (runNow) {
   		    start();
   		}
	}

    public void startSimulation(List nodeOrder, Map inputs, Map m_initState) {
        set_initialStates(nodeOrder, inputs, m_initState);
        start();
    }
    public void set_initialStates(List nodeOrder, Map inputs, Map m_initState) {
        initStatesIterator = new InitialStatesIterator(nodeOrder, inputs, m_initState);
    }
	public void interrupt() {
		ready = false;
	}

    /**
     * run the simulation in a new thread.
     */
    public void run() {
    	
    	try{
    		frame.endSimu( do_simulation());
    	}
    	catch ( GsException ge) {
    		GUIMessageUtils.openErrorDialog( "Unable to launch the simulation");
    		LogManager.error( "Unable to start Simulation");
		}
    }
	public Graph do_simulation() throws GsException {
        ready = true;
		boolean maxDepthReached = false;
		try {
			// iterate through initial states and run the simulation from each of them
			while(initStatesIterator.hasNext()) {
				// add the next proposed state
				queue.add(new SimulationQueuedState((byte[])initStatesIterator.next(), 0, null, false));
				
				// do the simulation itself
				while (!queue.isEmpty()) {
					SimulationQueuedState item = (SimulationQueuedState)(
							breadthFirst ? queue.removeFirst() 
										: queue.removeLast());

					if (helper.addNode(item)) {
						// this is a new node, increase node count, do some checks and so on
						nbnode++;
						if (nbnode % 100 == 0) {
						    if (frame != null) {
				                frame.setProgress(nbnode);
				            }
						}
						if (maxnodes != 0 && nbnode >= maxnodes){
							LogManager.error( "Maxnodes reached: " + maxnodes);
						    throw new GsException(GsException.GRAVITY_NORMAL, (String)null);
						}

//						// stop if it has been asked or if memory becomes unsufficient
//						if (ready && Runtime.getRuntime().freeMemory() < 5000) {
//							Runtime.getRuntime().gc();
//							if (Runtime.getRuntime().freeMemory() > 40000 ) {
//								System.out.println("out of memory: saved by garbage collector: "+nbgc);
//							} else {
//								GsEnv.error("out of memory, I'll stop to prevent loosing everything", null);
//								System.out.println("not ready anymore!!");
//								ready = false;
//							}
//						}
						if (!ready) {
						    throw new GsException(GsException.GRAVITY_NORMAL, Translator.getString("STR_interrupted"));
						}

						// run the simulation on the new node
						updater.setState(item.state, item.depth, helper.getNode());
						if (!updater.hasNext()) {
							helper.setStable();
							frame.addStableState(item);
							String display = "";
							for (int i=0 ; i<item.state.length ; i++ ) {
								display += item.state[i] + " ";
							}
							display += "\n";
							LogManager.trace( display, false); 
						} else {
							if (maxdepth == 0 || item.depth < maxdepth) {
								while (updater.hasNext()) {
									queue.addLast(updater.next());
								}
							} else {
								maxDepthReached = true;
							}
						}
					}
				}
			}
		} catch (GsException e) {
			LogManager.error( "Simulation was interrupted");
			LogManager.error( e);
		} catch (OutOfMemoryError e) {
			LogManager.error( "Out of Memory");
			LogManager.error( e);
		    GUIMessageUtils.openErrorDialog("Out Of Memory");
		    return null;
		} finally {
			if (maxDepthReached) {
				GUIMessageUtils.openErrorDialog("Reached the max depth");
				//TODO: explain what happened and give some hints
			}
		}
		return helper.endSimulation();
	}
}
