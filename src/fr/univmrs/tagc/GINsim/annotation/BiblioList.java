package fr.univmrs.tagc.GINsim.annotation;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;

import fr.univmrs.tagc.GINsim.graph.GsGraph;
import fr.univmrs.tagc.GINsim.graph.GsGraphNotificationAction;
import fr.univmrs.tagc.GINsim.graph.GsGraphNotificationMessage;
import fr.univmrs.tagc.common.GsException;
import fr.univmrs.tagc.common.OpenHelper;
import fr.univmrs.tagc.common.Tools;
import fr.univmrs.tagc.common.manageressources.Translator;
import fr.univmrs.tagc.common.xml.XMLHelper;
import fr.univmrs.tagc.common.xml.XMLWriter;
import fr.univmrs.tagc.common.xml.XMLize;


public class BiblioList implements XMLize, OpenHelper {

	Map files = new TreeMap();
	Map m_references = new HashMap();
	Map m_used = new HashMap();
	Ref curRef = null;
	GsGraph graph;
	
	public BiblioList(GsGraph graph) {
		this.graph = graph;
	}

	public void toXML(XMLWriter out, Object param, int mode) throws IOException {
		out.openTag("biblio");
		
		out.openTag("files");
		Iterator it = files.keySet().iterator();
		while (it.hasNext()) {
			out.addTag("file", new String[] {"filename", it.next().toString()});
		}
		out.closeTag();
		
		out.openTag("refs");
		it = m_used.keySet().iterator();
		while (it.hasNext()) {
			Ref ref = (Ref)m_references.get(it.next());
			if (ref != null) {
				ref.toXML(out, param, mode);
			}
		}
		out.closeTag();
		
		out.closeTag();
	}

	public void addRef(String key) {
		curRef = new Ref();
		setKey(key);
	}
	
	public void setKey(String key) {
		if (key == null || curRef == null || curRef.key != null) {
			return;
		}
		curRef.key = key;
		m_references.put(key, curRef);
	}
	
	public void addLinkToCurRef(String proto, String value) {
		if (curRef == null) {
			System.out.println("no current ref");
			return;
		}
		curRef.addLink(proto, value);
	}

	public void add(String proto, String value) {
		if (!proto.equals("ref")) {
			return;
		}
		m_used.put(value, null);
		if (!m_references.containsKey(value)) {
			addMissingRefWarning(value);
		}
	}

	protected void addFile() {
		JFileChooser jfc = new JFileChooser();
		int r = jfc.showOpenDialog(null);
		if (r != JFileChooser.APPROVE_OPTION) {
			return;
		}
		String fileName = jfc.getSelectedFile().getAbsolutePath();
		addFile(fileName);
	}

	public boolean open(String proto, String value) {
		if (!proto.equals("ref")) {
			return false;
		}
		Ref ref = (Ref)m_references.get(value);
		if (ref == null) {
			addMissingRefWarning(value);
			return false;
		}
		ref.open();
		return true;
	}

	public void addMissingRefWarning(String value) {
		// just in case: check if one of the source file has been updated
		Iterator it = files.entrySet().iterator();
		while (it.hasNext()) {
			Entry e = (Entry)it.next();
			Date d = (Date)e.getValue();
			if (d != null) {
				File f = new File((String)e.getKey());
				if (f.lastModified() > d.getTime()) {
					addFile(f.getAbsolutePath());
					if (m_references.containsKey(value)) {
						return;
					}
				}
			}
		}
		GsGraphNotificationAction action = new GsGraphNotificationAction() {
			String[] t = {Translator.getString("STR_addBib"), Translator.getString("STR_ignore")};
			public boolean timeout(GsGraph graph, Object data) {
				return true;
			}
		
			public boolean perform(GsGraph graph, Object data, int index) {
				switch (index) {
					case 0:
						((BiblioList)data).addFile();
						break;
					case 1:
						//((BiblioList)data).ignore();
						break;
				}
				return true;
			}
		
			public String[] getActionName() {
				return t;
			}
		
		};
		
		graph.addNotificationMessage(new GsGraphNotificationMessage(graph,
				Translator.getString("STR_noref"), 
			action, this,
			GsGraphNotificationMessage.NOTIFICATION_WARNING));
	}
	
	public String getLink(String proto, String value) {
		if (!proto.equals("ref")) {
			return null;
		}
		Ref ref = (Ref)m_references.get(value);
		if (ref == null) {
			return null;
		}
		// quick hack: take the first ignoring "file"
		Iterator it = ref.links.entrySet().iterator();
		while (it.hasNext()) {
			Entry e = (Entry)it.next();
			if (!"file".equals(e.getKey())) {
				return Tools.getLink(e.getKey(), e.getValue());
			}
		}
		return null;
	}

	public void addFile(String fileName) {
		files.put(fileName, new Date());
		
		File f = new File(fileName);
		if (f.exists()) {
			// FIXME: add a mechanism to support other biblio parsers
			new ReferencerParser(this, fileName);
		} else {
			GsGraphNotificationAction action = new GsGraphNotificationAction() {
				String[] t = {Translator.getString("STR_purge")};
				public boolean timeout(GsGraph graph, Object data) {
					return true;
				}
			
				public boolean perform(GsGraph graph, Object data, int index) {
					((BiblioList)((Object[])data)[0]).removeFile(((Object[])data)[1].toString());
					return true;
				}
			
				public String[] getActionName() {
					return t;
				}
			
			};
			
			graph.addNotificationMessage(new GsGraphNotificationMessage(graph,
					Translator.getString("STR_noBibFile"), 
				action, new Object[] {this, fileName},
				GsGraphNotificationMessage.NOTIFICATION_WARNING));
		}
	}
	
	public void removeFile(String fileName) {
		files.remove(fileName);
	}
}

class Ref {
	String key;
	Map links = new TreeMap();
	
	public void addLink(String proto, String value) {
		links.put(proto, value);
	}

	public void open() {
		if (links.containsKey("file") && Tools.openFile((String)links.get("file"))) {
			return;
		}
		Iterator it = links.entrySet().iterator();
		while (it.hasNext()) {
			Entry e = (Entry)it.next();
			Tools.open(e.getKey(), e.getValue());
		}
	}
	public void toXML(XMLWriter out, Object param, int mode) throws IOException {
		out.openTag("ref");
		out.addAttr("key", key);
		for (Iterator it=links.entrySet().iterator() ; it.hasNext() ; ) {
			Entry e = (Entry)it.next();
			out.addTag("link", new String[] {"key", e.getKey().toString(), "value", e.getValue().toString()});
		}
		out.closeTag();
	}
}



class ReferencerParser extends XMLHelper {

	String baseDir;
	BiblioList bibList;
	
	static final int DOC = 0;
	static final int KEY = 1;
	static final int DOI = 2;
	static final int FILENAME = 3;

	static Map CALLMAP = new TreeMap();
	static {
		addCall("doc", DOC, CALLMAP, STARTONLY, false);
		addCall("key", KEY, CALLMAP, ENDONLY, true);
		addCall("bib_doi", DOI, CALLMAP, ENDONLY, true);
		addCall("relative_filename", FILENAME, CALLMAP, ENDONLY, true);
		addCall("filename", FILENAME, CALLMAP, ENDONLY, true);
	}
	
    /**
     * @param graph expected node order
     */
    public ReferencerParser(BiblioList bibList, String path) {
    	this.bibList = bibList;
    	this.m_call = CALLMAP;
    	
		try {
			File f = new File(path);
			baseDir = f.getParent();
			startParsing(Tools.getStreamForPath(path), false);
		} catch (Exception e) {
			Tools.error(new GsException(GsException.GRAVITY_ERROR, e), null);
		}
    }
    
	protected void startElement(int id, Attributes attributes) {
		switch (id) {
			case DOC:
				bibList.addRef(null);
				break;
		}
	}

	protected void endElement(int id) {
		switch (id) {
			case KEY:
				bibList.setKey(curval);
				break;
			case DOI:
				if (curval.trim().length() > 0) {
					bibList.addLinkToCurRef("doi", curval);
				}
				break;
			case FILENAME:
				if (curval.startsWith("file:")) {
					curval = curval.substring(5);
				} else {
					curval = baseDir+File.separator+curval;
				}
				
				try {
					curval = URLDecoder.decode(curval, "utf8");
				} catch (UnsupportedEncodingException e) {
				}
				
				File f = new File(curval);
				if (f.exists()) {
					bibList.addLinkToCurRef("file", curval);
				} else {
					System.out.println("  could not find file "+curval);
				}
				break;
		}
	}
}