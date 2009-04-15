package fr.univmrs.tagc.GINsim.regulatoryGraph.initialState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import fr.univmrs.tagc.GINsim.global.GsEnv;
import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsGraphAssociatedObjectManager;
import fr.univmrs.tagc.GINsim.regulatoryGraph.GsRegulatoryGraph;
import fr.univmrs.tagc.common.GsException;
import fr.univmrs.tagc.common.xml.XMLHelper;
import fr.univmrs.tagc.common.xml.XMLWriter;

public class GsInitialStateManager implements GsGraphAssociatedObjectManager {

	public static final String key = "initialState";
	
    public Object doOpen(InputStream is, GsGraph graph) {
        initStateParser parser = new initStateParser((GsRegulatoryGraph)graph);
        parser.startParsing(is, false);
        return parser.getParameters();
    }

    public void doSave(OutputStreamWriter os, GsGraph graph) {
        GsInitialStateList imanager = (GsInitialStateList)graph.getObject(key, true);
        List nodeOrder = graph.getNodeOrder();
        if (imanager == null || imanager.isEmpty() || nodeOrder == null || nodeOrder.size() == 0) {
            return;
        }
        try {
            XMLWriter out = new XMLWriter(os, null);
            out.openTag("initialStates");
            imanager.getInitialStates().toXML(out, "initialState");
            imanager.getInputConfigs().toXML(out, "input");
            out.closeTag();
        } catch (IOException e) {
            GsEnv.error(new GsException(GsException.GRAVITY_ERROR, e.getLocalizedMessage()), null);
        }
    }

	public String getObjectName() {
		return key;
	}

    public boolean needSaving(GsGraph graph) {
        GsInitialStateList list = (GsInitialStateList)graph.getObject(key, false);
        return list != null && !list.isEmpty();
    }

	public Object doCreate(GsGraph graph) {
		return new GsInitialStateList(graph);
	}
}


class initStateParser extends XMLHelper {

    GsInitialStateList imanager;
    InitialStateList list, inputs;
    
    /**
     * @param graph expected node order
     */
    public initStateParser(GsRegulatoryGraph graph) {
        imanager = new GsInitialStateList(graph);
        list = imanager.getInitialStates();
        inputs = imanager.getInputConfigs();
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        
        if ("initialState".equals(qName)) {
            int index = list.add();
            GsInitialState istate = (GsInitialState)list.getElement(null, index);
            istate.setData(attributes.getValue("value").trim().split(" "), imanager.normalNodes);
            istate.name = attributes.getValue("name").trim();
        }
        if ("input".equals(qName)) {
            int index = inputs.add();
            GsInitialState istate = (GsInitialState)inputs.getElement(null, index);
            istate.setData(attributes.getValue("value").trim().split(" "), imanager.inputNodes);
            istate.name = attributes.getValue("name").trim();
        }
    }
    
    /**
     * @return the list of parameters read by this parser.
     */
	public Object getParameters() {
		return imanager;
	}
}
