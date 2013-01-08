package org.ginsim.service.tool.localgraph;

import java.util.List;
import java.util.Map;

import org.colomoto.logicalmodel.LogicalModel;
import org.ginsim.common.application.GsException;
import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.core.graph.regulatorygraph.RegulatoryMultiEdge;
import org.ginsim.core.graph.regulatorygraph.perturbation.Perturbation;
import org.ginsim.core.service.Alias;
import org.ginsim.core.service.Service;
import org.ginsim.service.tool.reg2dyn.updater.SynchronousSimulationUpdater;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(Service.class)
@Alias("localgraph")
public class LocalGraphService implements Service {

	public Map<RegulatoryMultiEdge, String> run(RegulatoryGraph graph,
			List<byte[]> selStates) throws GsException {
		return this.run(graph, selStates, null);
	}

	public Map<RegulatoryMultiEdge, String> run(RegulatoryGraph graph,
			List<byte[]> selStates, Perturbation mutant) throws GsException {
		if (selStates == null) {
			throw new GsException(GsException.GRAVITY_NORMAL,
					"You must select at least one state");
		}

		LogicalModel model = graph.getModel();
		if (mutant != null) {
			mutant.update(model);
		}

		LocalGraph lg = new LocalGraph(graph, selStates);
		lg.setUpdater(new SynchronousSimulationUpdater(model));
		Map<RegulatoryMultiEdge, String> functionalityMap = lg.run();
		return functionalityMap;
	}
}
