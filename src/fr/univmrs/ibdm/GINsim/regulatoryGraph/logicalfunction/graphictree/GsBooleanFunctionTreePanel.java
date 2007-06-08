package fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTree;

import fr.univmrs.ibdm.GINsim.regulatoryGraph.logicalfunction.graphictree.datamodel.GsTreeElement;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

public abstract class GsBooleanFunctionTreePanel extends JPanel {
  protected static Font defaultFont = new Font("monospaced", Font.PLAIN, 10);
  protected GsTreeElement treeElement;
  protected JTree tree = null;
  protected static int charWidth = 8, charHeight = 8;
  protected int width = 0;
  protected boolean selected;
  protected String text;
  protected MouseListener mouseListener;
  protected MouseMotionListener mouseMotionListener;

  public GsBooleanFunctionTreePanel(Object value, JTree tree, boolean sel, int w) {
    super();
    setBackground(Color.white);
    treeElement = (GsTreeElement)value;
    this.tree = tree;
    selected = sel;
    width = w;
    text = treeElement.toString();
    setLayout(new GridBagLayout());
  }
  public void paint(Graphics g) {
    charWidth = g.getFontMetrics(defaultFont).charWidth('A');
    charHeight = g.getFontMetrics(defaultFont).getHeight();
    super.paint(g);
    if (selected) {
      g.setColor(Color.blue);
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
    if (treeElement.isDropable()) {
      g.setColor(Color.red);
      //((Graphics2D)g).setStroke(new BasicStroke(2));
      g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
  }
  public void setMouseListener(MouseListener ml) {
    mouseListener = ml;
  }
  public void setMouseMotionListener(MouseMotionListener mml) {
    mouseMotionListener = mml;
  }
  public MouseListener getMouseListener() {
    return mouseListener;
  }
  public MouseMotionListener getMouseMotionListener() {
    return mouseMotionListener;
  }
}
