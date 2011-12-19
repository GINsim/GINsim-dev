package org.ginsim.core.graph.tree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ginsim.common.ColorPalette;
import org.ginsim.core.graph.regulatorygraph.RegulatoryNode;
import org.ginsim.core.graph.view.EdgeAttributesReader;
import org.ginsim.core.graph.view.EdgePattern;
import org.ginsim.core.graph.view.NodeAttributesReader;
import org.ginsim.core.graph.view.NodeBorder;
import org.ginsim.core.graph.view.NodeShape;
import org.ginsim.service.tool.circuit.OmsddNode;


/**
 * A treeParser for the context of functionality
 *
 */
public class TreeBuilderFromCircuit extends TreeBuilder {
	protected final static int debug = 1;
	
	
	/**
	 * Indicates the maximum count of terminal.
	 */
	protected int max_terminal;
	/**
	 * Indicates for each depth, the sub-total (width) of children or 0 if the depth is not skipped (realDepth == -1).
	 */
	protected int[] widthPerDepth;
	/**
	 * Indicates for each depth, the total (width) of children or 0 if the depth is not skipped (realDepth == -1).
	 */
	protected int[] widthPerDepth_acc;
	/**
	 * As the omdd diagram could not contain all the levels (some could be skipped...), 
	 * we try to reduce the tree width, by assigning a depth to each __used__ level.
	 * 
	 * This can be achieved in two ways, 
	 * 		* by a first pass on the omdd (good for diagram)
	 *      * by computing the incoming vertices from the regulatoryGraph (good for tree)
	 *      
	 * A value of -2, indicates the depth correspond to a skipped level.
	 */
	protected int[] realDetph;
	/**
	 * The total number of levels that are not skipped
	 */
	protected int total_levels;
	/**
	 * The level of the last depth (corresponding to the terminal node in widthPerDepth)
	 */
	protected int max_depth;

	protected OmsddNode root;
	
	public static final String PARAM_INITIALCIRCUITDESC = "pcirc_initialCircuitDescr";
	public static final String PARAM_ALLCONTEXTS = "pcirc_allCircuits";
	
	protected List allCircuits;

	public void init() {
		allCircuits =  (List) getParameter(PARAM_ALLCONTEXTS);
		root = (OmsddNode) getParameter(PARAM_INITIALCIRCUITDESC);
		nodeOrder = (List)getParameter(PARAM_NODEORDER);

		widthPerDepth = widthPerDepth_acc = realDetph = null;
		total_levels = max_depth = 0;
		max_terminal = 3;
		initRealDepth(root);
	}

	
	/**
	 * Initialize the <b>realDepth</b> array, and <b>max_terminal</b> from an initial node, assuming regGraph is defined
	 * @param root
	 */
	public void initRealDepth(OmsddNode root) {
		realDetph = new int[nodeOrder.size()+1]; //+1 for the leafs
		_initRealDepth(root);
		int next_realDepth = 0;
		for (int i = 0; i < realDetph.length; i++) {
			if (realDetph[i] == -1) {
				total_levels++;
				realDetph[i] = next_realDepth++;
			} else realDetph[i] = -2;
		}
	}
    public void _initRealDepth(OmsddNode o) {
        if (o.next == null) {
            return ;
        }
        realDetph[o.level] = -1;
        for (int i = 0 ; i < o.next.length ; i++) {
            _initRealDepth(o.next[i]);
        }
    }

	

	/**
	 * Return the name of the node at level
	 * @param level the level of the node
	 * @return the name
	 */
	protected String getNodeName(int level) {
		return ((RegulatoryNode)nodeOrder.get(level)).getId();
	}
	
	public void parseOmdd() {
		if (tree.getMode() == TreeImpl.MODE_TREE) {
			createTreeFromOmdd(root);
		} else {
			createDiagramFromOmdd(root);
		}	
	}

	public void updateLayout(NodeAttributesReader vreader, TreeNode vertex) {
		vreader.setNode(vertex);
		int total_width = getTerminalWidth()*TreeNode.PADDING_HORIZONTAL;
		if (vertex.getType() == TreeNode.TYPE_LEAF) {
			vreader.setShape(NodeShape.ELLIPSE);
			vreader.setBackgroundColor(ColorPalette.defaultPalette[vertex.getValue()+1]);
			vreader.setBorder(NodeBorder.SIMPLE);
			if (vertex.getDepth() != -1) {
	    		vreader.setPos((int)((vertex.getWidth()-0.5)*total_width/getWidthPerDepth_acc(vertex))+100, getTotalLevels()*TreeNode.PADDING_VERTICAL+40);
			} else {
	    		vreader.setPos((int)((vertex.getWidth()+0.5)*total_width/getMaxTerminal())+100, getTotalLevels()*TreeNode.PADDING_VERTICAL+40);
			}
		} else {
			vreader.setShape(NodeShape.RECTANGLE);			
			if (vertex.getValue() == TreeNode.SKIPPED) {
				vreader.setBackgroundColor(Color.WHITE);
				vreader.setForegroundColor(Color.GRAY);
			}
			else vreader.setBackgroundColor(ColorPalette.defaultPalette[0]);
			vreader.setPos((int)((vertex.getWidth()-0.5)*total_width/getWidthPerDepth_acc(vertex))+100, (getRealDepth(vertex)+1)*TreeNode.PADDING_VERTICAL-40);
		}
		vreader.refresh();
	}

	
	public void computeWidthPerDepthFromRegGraph() {
		widthPerDepth = new int[nodeOrder.size()+1];
		widthPerDepth_acc = new int[nodeOrder.size()+1];

		int last_real = -1;

		Iterator it = nodeOrder.iterator();
		for (int i = 0 ; it.hasNext() ; i++) {
			RegulatoryNode v = (RegulatoryNode) it.next();
			if (realDetph[i] != -2) {
				int max = v.getMaxValue()+1;
				widthPerDepth[i] = max;
				if (last_real != -1) widthPerDepth_acc[i] = widthPerDepth_acc[last_real] * widthPerDepth[last_real];
				else widthPerDepth_acc[i] = 1;
				last_real = i;
			}
		}
		max_depth = last_real+1;
		if (last_real != -1) widthPerDepth_acc[max_depth] = widthPerDepth_acc[last_real] * widthPerDepth[last_real];
	}	
	
	public void createDiagramFromOmdd(OmsddNode root) {
		computeWidthPerDepthFromRegGraph();
		int[] currentWidthPerDepth = new int[widthPerDepth.length];
		tree.setRoot( _createDiagramFromOmdd(root, 0, currentWidthPerDepth, tree.getEdgeAttributeReader()));
	}
	private TreeNode _createDiagramFromOmdd(OmsddNode o, int lastLevel, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		TreeNode treeNode;
		int mult;
		if (o.next == null) {
			mult = jump(lastLevel, max_depth, currentWidthPerDepth);
		
			if (tree.getMode() == TreeImpl.MODE_DIAGRAM_WITH_MULTIPLE_LEAFS) {
				treeNode = new TreeNode(""+o.value, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, o.value);
				if (mult > 1) currentWidthPerDepth[max_depth] += mult-1;
				tree.addNode(treeNode);
			} else { // if (mode == MODE_DIAGRAM) {
				if (o.value == -1) treeNode =  TreeImpl.MINUS_ONE_NODE;
				else treeNode = TreeImpl.leafs[o.value];
				if (!tree.containsNode(treeNode)) {
					tree.addNode(treeNode);
				}
			}
			return treeNode;
		}
		
		mult = jump(lastLevel, o.level, currentWidthPerDepth);

		treeNode = new TreeNode(getNodeName(o.level), o.level, ++currentWidthPerDepth[o.level], TreeNode.TYPE_BRANCH); 
		tree.addNode(treeNode);
	
		for (int i = 0 ; i < o.next.length ; i++) { //For all the children
	    	TreeNode child = _createDiagramFromOmdd(o.next[i], o.level, currentWidthPerDepth, ereader);
	    	linkNode(treeNode, child, i, ereader);
	    }
		
		if (mult > 1) {
			mult = jump(o.level, max_depth, currentWidthPerDepth);
			if (mult > 1) {
				currentWidthPerDepth[max_depth] += mult;
			}
		}
	    return treeNode;
	}
	
	public void createTreeFromOmdd(OmsddNode root) {
		computeWidthPerDepthFromRegGraph();
		int[] currentWidthPerDepth = new int[widthPerDepth.length];
		tree.setRoot ((TreeNode) _createTreeFromOmdd(root, 0, null, 0, currentWidthPerDepth, tree.getEdgeAttributeReader()).get(0));
	}
	private List _createTreeFromOmdd(OmsddNode o, int lastLevel, TreeNode parent, int childIndex, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		TreeNode treeNode = null;
		List parents = new ArrayList();
		parents.add(parent);
		int mult, last_real;
		if (o.next == null) {
			mult = 1;

			last_real = 0;
			for (int j = lastLevel+1 ; j < max_depth ; j++) { //For all the missing genes
				if (realDetph[j] != -2) {
					parents = addChildren(j, mult, parents, childIndex, currentWidthPerDepth, ereader);
					mult = widthPerDepth[j];
					last_real = j;
				}
			}
			for (Iterator it = parents.iterator(); it.hasNext();) {
				TreeNode p = (TreeNode) it.next();
				if (mult > 1) {
					for (int i = 0; i < widthPerDepth[last_real]; i++) {
						treeNode = new TreeNode(""+o.value, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, o.value);
						tree.addNode(treeNode);
				    	linkNode(p, treeNode, i, ereader);
					}
				} else {
					treeNode = new TreeNode(""+o.value, max_depth, ++currentWidthPerDepth[max_depth], TreeNode.TYPE_LEAF, o.value);
					tree.addNode(treeNode);
			    	linkNode(p, treeNode, childIndex, ereader);	
				}
			}
			return null;
		}
		
		mult = 1;
		last_real = 0;
		List skippedParents = parents;
		for (int j = lastLevel+1 ; j < o.level ; j++) { //For all the missing genes
			if (realDetph[j] != -2) {
				skippedParents = addChildren(j, mult, skippedParents, childIndex, currentWidthPerDepth, ereader);
				mult = widthPerDepth[j];
				last_real = j;
			}
		}
		List currentNodes = new ArrayList();
		int nodeCountToCreate = 1;
		if (mult > 1) nodeCountToCreate = widthPerDepth[last_real];
		for (int k = 0; k < skippedParents.size(); k++) {
			for (int i = 0; i < nodeCountToCreate; i++) {
				treeNode = new TreeNode(getNodeName(o.level), o.level, ++currentWidthPerDepth[o.level], TreeNode.TYPE_BRANCH); 
				tree.addNode(treeNode);
				currentNodes.add(treeNode);
		
				for (int j = 0 ; j < o.next.length ; j++) { //For all the children
					if (o.next[j] != null) {
				    	List childs = _createTreeFromOmdd(o.next[j], o.level, treeNode, j, currentWidthPerDepth, ereader);
						if (childs != null) {
							for (Iterator it2 = childs.iterator(); it2 .hasNext();) {
								TreeNode child = (TreeNode) it2.next();
								linkNode(treeNode, child, j, ereader);
								
							}
						}
					}
			    }
			}
		}

		if (skippedParents.equals(parents)) {
		    return currentNodes;
		} else {
			int max = currentNodes.size()/skippedParents.size();
			for (int i = 0; i < skippedParents.size(); i++) {
				TreeNode p = (TreeNode)skippedParents.get(i);
				for (int j = 0; j < max; j++) {
					TreeNode child = (TreeNode)currentNodes.get(j+i*max);
					linkNode(p, child, j, ereader);
				}
			}
			return null;
		}
	}

	
	private List addChildren(int j, int mult, List parents, int childIndex, int[] currentWidthPerDepth, EdgeAttributesReader ereader) {
		List newParents = new ArrayList(mult);
		
		while (realDetph[j] == -2 && j < max_depth) j++; //Get the child level
		
		String parentId = getNodeName(j);
		
		for (Iterator it = parents.iterator(); it.hasNext();) {
			TreeNode o  = (TreeNode) it.next();
			for (int i = 0 ; i < mult ; i++) {
				TreeNode treeNode = new TreeNode(parentId, j, ++currentWidthPerDepth[j], TreeNode.TYPE_BRANCH, TreeNode.SKIPPED);
				newParents.add(treeNode);
				tree.addNode(treeNode);
				linkNode(o, treeNode, childIndex, ereader);
			}
		}
		return newParents;
	}

	/**
	 * Try to jump over the skipped nodes between lastLevel and maxLevel and fill currentWidthPerDepth accordingly.
	 * @param lastLevel
	 * @param maxLevel
	 * @param currentWidthPerDepth
	 * @return the max width of skipped nodes
	 */
	private int jump(int lastLevel, int maxLevel, int[] currentWidthPerDepth) {
		int mult = 1;
		for (int j = lastLevel+1 ; j < maxLevel ; j++) { //For all the missing genes
			if (realDetph[j] != -2) {
				currentWidthPerDepth[j] += mult;
				mult *= widthPerDepth[j];
			}
		}
		return mult;
	}
	
	/**
	 * Create an edge from source to target
	 * Also set the right color according to colorIndex, and the dashed line if the target is a leaf.
	 * 
	 * @param source
	 * @param target
	 * @param colorIndex
	 * @param ereader
	 */
	private void linkNode(TreeNode source, TreeNode target, int colorIndex, EdgeAttributesReader ereader) {
		Object e = tree.addEdge(source, target);
		ereader.setEdge(e);
		ereader.setLineColor(ColorPalette.defaultPalette[colorIndex+1]);
    	if (target.isLeaf()) {
    		ereader.setDash(EdgePattern.DASH);
    	}
		ereader.refresh();
	}

	public int[] getWidthPerDepth() { return widthPerDepth; }
	public int[] getWidthPerDepth_acc() { return widthPerDepth_acc; }
	public int getMaxTerminal() { return max_terminal; }
	public int getMaxDepth() { return max_depth; }
	public int[] getRealDetph() { return realDetph; }
	public int getTerminalWidth() { return widthPerDepth_acc[max_depth]; }
	public int getTotalLevels() { return total_levels; }
	
	protected int getRealDepth(TreeNode node) {
		if (node.getDepth() == TreeNode.LEAF_DEFAULT_DEPTH) return getMaxDepth();
		return getRealDetph()[node.getDepth()];
	}
	protected int getWidthPerDepth_acc(TreeNode node) {
		if (node.getDepth() == TreeNode.LEAF_DEFAULT_DEPTH) return getWidthPerDepth_acc()[getMaxDepth()];
		return getWidthPerDepth_acc()[node.getDepth()];
	}

}
