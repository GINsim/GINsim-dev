package org.ginsim.service.export.cadp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;

public class CADPSvlWriter extends CADPWriter {

	public CADPSvlWriter(CADPExportConfig config) {
		super(config);
	}

	public String toString() {
		String out = "";

		// Generate all models
		for (int i = 1; i <= this.getNumberInstances(); i++) {
			out += "\""
					+ this.getBCGModelFileName(i)
					+ "\""
					+ " = safety reduction of tau*.a reduction of branching reduction of generation of hide all but ";
			int index = 0;
			for (RegulatoryNode visible : this.getListVisible()) {
				if (index++ > 0)
					out += ", ";
				out += CADPWriter.node2Gate(visible, i);
			}

			if (index > 0)
				out += ", ";

			GateWriter gateWriter = this.getGateWriter();

			out += CADPWriter.getStableActionName() + " in ";
			out += "\"" + this.getLNTModelFileName() + "\":"
					+ this.concreteProcessName(i) + "["
					+ gateWriter.simpleListWithModuleId(i) + "]" + ";\n";
		}

		// Generate all integration processes
		for (int i = 1; i <= this.getNumberInstances(); i++) {
			for (RegulatoryNode input : this.getMappedInputs()) {
				out += "\""
						+ this.getBCGIntegrationFileName(input, i)
						+ "\""
						+ " = safety reduction of tau*.a reduction of branching reduction of generation of ";

				List<String> gateList = new ArrayList<String>();
				Collection<RegulatoryNode> listProper = this
						.getProperComponentsForInput(input);
				for (int j = 1; j <= this.getNumberInstances(); j++)
					if (this.areNeighbours(i, j))
						for (RegulatoryNode proper : listProper)
							gateList.add(CADPWriter.node2SyncAction(input, i,
									proper, j));

				out += "\"" + this.getLNTIntegrationFileName() + "\":"
						+ this.concreteIntegrationProcessName(input, i) + "["
						+ CADPWriter.makeCommaList(gateList) + "]" + ";\n";

			}
		}

		out += "\"" + "composition_" + this.getModelName() + "_"
				+ this.getNumberInstances() + ".bcg" + "\"";
		out += " = safety reduction of tau*.a reduction of smart branching reduction of \""
				+ this.getExpFileName() + "\";\n";

		return out;
	}

}
