package org.ginsim.servicegui.tool.composition.integrationgrammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.colomoto.logicalmodel.NodeInfo;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationAtom;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationExpression;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationNegation;
import org.ginsim.servicegui.tool.composition.integrationgrammar.IntegrationFunctionSpecification.IntegrationOperation;

public class IntegrationFunctionDNFFactory {

	private CompositionContext context = null;

	public IntegrationFunctionDNFFactory(CompositionContext context) {
		this.context = context;
	}

	public IntegrationFunctionClauseSet getClauseSet(
			IntegrationExpression expression, int instance) {

		if (expression instanceof IntegrationOperation) {
			List<IntegrationExpression> listOperands = ((IntegrationOperation) expression)
					.getOperands();

			IntegrationLogicalOperator operator = ((IntegrationOperation) expression)
					.getOperation();

			switch (operator) {
			case AND: {
				IntegrationFunctionClauseSet intermediate = new IntegrationFunctionClauseSet();
				intermediate.setTautological();
				for (IntegrationExpression operand : listOperands)
					intermediate
							.conjunctionWith(getClauseSet(operand, instance));

				return intermediate;
			}
			case OR: {
				IntegrationFunctionClauseSet intermediate = new IntegrationFunctionClauseSet();
				for (IntegrationExpression operand : listOperands)
					intermediate
							.disjunctionWith(getClauseSet(operand, instance));

				return intermediate;
			}
			default:
				return new IntegrationFunctionClauseSet();

			}
		} else if (expression instanceof IntegrationNegation) {
			IntegrationFunctionClauseSet toNegate = getClauseSet(
					((IntegrationNegation) expression).getNegatedExpression(),
					instance);

			return toNegate.negate();

		} else if (expression instanceof IntegrationAtom) {
			IntegrationAtom atom = (IntegrationAtom) expression;

			byte threshold = atom.getThreshold();
			int min = atom.getMinNeighbours();
			int max = atom.getMaxNeighbours();
			int distance = atom.getDistance();

			Set<Integer> neighbours = context.getNeighbourIndices(instance,
					distance);
			List<Integer> nList = new ArrayList<Integer>();
			for (Integer neighbour : neighbours)
				nList.add(neighbour);

			if (min == -1 || min > neighbours.size())
				min = neighbours.size();

			if (max == -1 || max > neighbours.size())
				max = neighbours.size();

			IntegrationFunctionClauseSet result = new IntegrationFunctionClauseSet();

			for (int v = min; v <= max; v++) {
				List<boolean[]> masks = generateNeighboursMask(v, nList);

				for (boolean[] mask : masks) {
					IntegrationFunctionClauseSet conjunction = new IntegrationFunctionClauseSet();
					conjunction.setTautological();

					for (int i = 0; i < mask.length; i++) {

						if (mask[i]) {
							NodeInfo node = context
									.getLowLevelComponentFromName(
											atom.getComponentName(),
											nList.get(i).intValue());
							IntegrationFunctionClauseSet disjunction = new IntegrationFunctionClauseSet();

							for (byte l = threshold; l <= node.getMax(); l++) {
								IntegrationFunctionClause clause = new IntegrationFunctionClause();

								clause.addConstraint(node, l);
								disjunction.disjunctionWith(clause);

							}
							conjunction.conjunctionWith(disjunction);

						}

						result.disjunctionWith(conjunction);

					}
				}

			}

			return result;

		} else {
			// a null operand denotes an inexistent operand with
			// an empty set of associated clauses
			IntegrationFunctionClauseSet tautology = new IntegrationFunctionClauseSet();
			tautology.setTautological();

			return tautology;
		}

	}

	private static List<boolean[]> generateNeighboursMask(int v,
			List<Integer> neighbours) {
		boolean[] mask = new boolean[neighbours.size()];
		for (int i = 0; i < mask.length; i++)
			mask[i] = false;
		return generateNeighboursMask(v, neighbours.size(), 0, mask);
	}

	private static List<boolean[]> generateNeighboursMask(int n, int m,
			int offset, boolean[] frozen) {
		List<boolean[]> masks = new ArrayList<boolean[]>();

		int places = m - offset;

		if (n == 0) {
			boolean[] mask = new boolean[m];
			for (int i = 0; i < m; i++)
				mask[i] = frozen[i];
			masks.add(mask);
		} else if (places == n) {
			boolean[] mask = new boolean[m];
			for (int i = 0; i < m; i++) {
				if (i < offset) {
					mask[i] = frozen[i];
				} else {
					mask[i] = true;
				}
			}

			masks.add(mask);
		} else {
			boolean[] mask = new boolean[m];
			for (int i = 0; i < m; i++) {
				if (i < offset) {
					mask[i] = frozen[i];
				}
			}

			mask[offset] = true;
			masks.addAll(generateNeighboursMask(n - 1, m, offset + 1, mask));
			mask[offset] = false;
			masks.addAll(generateNeighboursMask(n, m, offset + 1, mask));

		}

		return masks;
	}

}
