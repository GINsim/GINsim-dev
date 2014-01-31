package org.ginsim.service.tool.connectivity;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.graph.view.style.StyleProvider;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.EStatus;
import org.ginsim.core.service.Service;
import org.ginsim.core.service.ServiceStatus;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(Service.class)
@ServiceStatus(EStatus.DEPRECATED)
public class ConnectivityService implements Service {

	/**
	 * Compute the SCC of graph in another thread.
	 *
	 *
	 * @param graph the graph to compute the SCC on
	 * @return a ConnectivityResult object holding the SCC
	 */
    public ConnectivityResult run(Graph graph) {
        return run(graph, true);
    }

    public ConnectivityResult getSCC(Graph graph) {
        return run(graph, false);
    }
	/**
	 * Compute the SCC of graph in another thread.
	 *
	 *
	 * @param graph the graph to compute the SCC on
	 * @param inThread indicates if the algo should run on a separate thread
	 * @return a ConnectivityResult object holding the SCC
	 */
	public ConnectivityResult run(Graph graph, boolean inThread) {
		ConnectivityAlgo algo = new ConnectivityAlgo();
		ConnectivityResult result = algo.configure(graph);
		result.setAlgo(algo);
		if (inThread) {
			Thread th = new Thread(algo);
			th.run();
		} else {
			algo.run();
		}
		return result;
	}

    public StyleProvider getStyleProvider(ConnectivityResult result, Graph graph) {
        return new ConnectivityStyleProvider(result.getComponents(), graph);
    }

}
