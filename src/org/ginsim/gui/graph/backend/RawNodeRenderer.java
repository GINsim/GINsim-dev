package org.ginsim.gui.graph.backend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JLabel;

import org.ginsim.core.graph.view.NodeAttributesReader;
import org.jgraph.JGraph;


class RawNodeRenderer extends JLabel {
	private static final long serialVersionUID = -1665537691798585356L;
	
	public static final int SW = 6;      // width of the selection mark
	public static final int hSW = SW/2;  // half width of the selection mark
	
	private final NodeAttributesReader reader;
	private final JGraph jgraph;
	
	private boolean selected;

	private boolean dirty = false;
	
	public RawNodeRenderer(JGraph jgraph, NodeAttributesReader vertexAttributeReader) {
		this.reader = vertexAttributeReader;
		this.jgraph = jgraph;
		setHorizontalTextPosition(CENTER);
		setHorizontalAlignment(CENTER);
	}

	public void setView(RawNodeView view, boolean selected, boolean preview) {

		setText(view.toString());
		this.selected = selected;
		
		Rectangle rect = view.getBounds();
		super.setBounds(rect.x, rect.y, rect.width, rect.height);
		reader.setNode(view.user);
		
		if (dirty && !preview) {
			dirty = false;
			System.out.println("Forcing refresh.. why does it fail?");
			jgraph.refresh();
			jgraph.graphDidChange();
		}
	}
	
	public Rectangle getBounds(Object user) {
		reader.setNode(user);
		
		int x = reader.getX()-hSW;
		int y = reader.getY()-hSW;
		int w = reader.getWidth() + SW;
		int h = reader.getHeight() + SW;
		return new Rectangle(x,y, w,h);
	}

	@Override
	public void paint(Graphics g) {
		int w = getWidth()-SW;
		int h = getHeight()-SW;
		Shape s = reader.getShape().getShape(hSW, hSW, w, h);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(reader.getBackgroundColor());
		g2d.fill(s);
		g2d.setColor(reader.getForegroundColor());
		g2d.draw(s);

		if (selected) {
			g2d.setColor(Color.red);
			g2d.fillRect(0, 0, SW, SW);
			g2d.fillRect(0, h, SW, SW);
			g2d.fillRect(w, h, SW, SW);
			g2d.fillRect(w, 0, SW, SW);
		}

		super.paint(g);
	}

	public void translate(Object user, double dx, double dy) {
		
		reader.setNode(user);
		
		int x = reader.getX();
		int y = reader.getY();
		int w = reader.getWidth();
		int h = reader.getHeight();

		Rectangle oldBounds = new Rectangle(x, y, w, h);
		reader.setPos(x+(int)dx, y+(int)dy);
		
		dirty = true;
	}
}
