package fr.univmrs.ibdm.GINsim.graph;

import java.awt.Color;
import java.util.Map;

/**
 * a generic vertexAttributeReader storing data into a dedicated hashmap
 */
public class GsFallbackVertexAttributeReader extends GsVertexAttributesReader {

    private Map dataMap = null;
    private VertexVSdata vvsd;
    
    /**
     * @param map
     */
    public GsFallbackVertexAttributeReader(Map map) {
        this.dataMap = map;
    }

    public void setVertex(Object vertex) {
        vvsd = (VertexVSdata)dataMap.get(vertex);
        if (vvsd == null) {
            vvsd = new VertexVSdata();
            vvsd.bgcolor = bg;
            vvsd.fgcolor = fg;
            vvsd.border = border;
            vvsd.w = width;
            vvsd.h = height;
            vvsd.shape = shape;
            dataMap.put(vertex, vvsd);
        }
    }

    public int getX() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.x;
    }

    public int getY() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.y;
    }

    public int getHeight() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.h;
    }

    public int getWidth() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.w;
    }

    public Color getForegroundColor() {
        if (vvsd == null) {
            return null;
        }
        return vvsd.fgcolor;
    }

    public void setForegroundColor(Color color) {
        if (vvsd == null) {
            return;
        }
        vvsd.fgcolor = color;
    }

    public Color getBackgroundColor() {
        if (vvsd == null) {
            return null;
        }
        return vvsd.bgcolor;
    }

    public void setBackgroundColor(Color color) {
        if (vvsd == null) {
            return;
        }
        vvsd.bgcolor = color;
    }

    public void refresh() {
    }

    public void setPos(int x, int y) {
        if (vvsd == null) {
            return;
        }
        vvsd.x = x;
        vvsd.y = y;
    }

    public void setSize(int w, int h) {
        if (vvsd == null) {
            return;
        }
        vvsd.w = w;
        vvsd.h = h;
    }

    public void setBorder(int index) {
        if (vvsd == null) {
            return;
        }
        vvsd.border = index;
    }

    public int getBorder() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.border;
    }

    public int getShape() {
        if (vvsd == null) {
            return 0;
        }
        return vvsd.shape;
    }

    public void setShape(int shapeIndex) {
        if (vvsd == null) {
            return;
        }
        vvsd.shape = shapeIndex;
    }

    
    class VertexVSdata {
        protected int x, y, w, h;
        protected Color fgcolor;
        protected Color bgcolor;
        
        protected int shape;
        protected int border;
    }
}
