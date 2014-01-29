package org.ginsim.core.graph.view.style;

import java.awt.Color;

import org.ginsim.core.graph.Edge;
import org.ginsim.core.graph.view.EdgeEnd;
import org.ginsim.core.graph.view.EdgePattern;

/**
 * Define the visual parameters of an edge.
 * 
 * @author Aurelien Naldi
 *
 * @param <V>
 * @param <E>
 */
public interface EdgeStyle<V,E extends Edge<V>> extends Style {

	Color getColor(E edge);
	int getWidth(E edge);
	EdgePattern getPattern(E edge);
	EdgeEnd getEnding(E edge);

	
	boolean enforceColor();
	boolean enforceEnding();
	boolean enforcePattern();
	boolean enforceWidth();

	boolean matches(Color color, EdgePattern pattern, int width);

}
