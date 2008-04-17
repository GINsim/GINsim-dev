package fr.univmrs.tagc.common.document;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;


public class WikiDocumentWriter extends DocumentWriter {

	OutputStreamWriter writer;
	
	Map m_style = new HashMap();
	Vector v_table = new Vector();
	public String NEW_LINE = "\n";
	
	Stack lists = new Stack();
	String curList = null;
	

	public void startDocument() throws IOException {
		writer = new OutputStreamWriter(output, "UTF-8");
	}
	
	protected void doOpenParagraph(String style) throws IOException {
		writer.write("\n\n");
	}
	
	protected void doWriteText(String text, boolean newLine) throws IOException {
		writer.write(text);
		if (newLine) {
			writer.write("\n");
		}
	}
	
	protected String newLine() {
		return NEW_LINE;
	}

	protected void doOpenTable(String name, String style, String[] t_colStyle) throws IOException {
		writer.write("{{table}}\n");
	}
	
	protected void doCloseTable() throws IOException {
		writer.write("{{/table}}\n");
	}
	
	protected void doOpenTableRow() throws IOException {
		writer.write("|--------\n");
	}
	
	protected void doOpenTableCell(int colspan, int rowspan) throws IOException {
		writer.write("| ");
	}
	
	protected void doCloseDocument() throws IOException {
		writer.flush();
		writer.close();
	}

	protected void doCloseParagraph() throws IOException {
		writer.write("\n");
		writer.write("\n");
	}

	protected void doCloseTableCell() throws IOException {
		writer.write("\n");
	}

	protected void doCloseTableRow() throws IOException {
	}
	
	protected void doOpenHeader(int level, String content, String style) throws IOException {
		String s = "";
		for (int i=0 ; i<level ; i++) {
			s += "=";
		}
		writer.write(s + " " + content + "\n");
	}
	
	protected void doAddLink(String href, String content) throws IOException {
		writer.write("[["+href+"|"+content+"]]");
	}
	protected void doOpenList(String style) throws IOException {
		boolean numbered = false;
		if (style != null) {
			Map m_style = documentStyles.getPropertiesForStyle(style);
			if (m_style != null) {
				numbered = "O".equals(m_style.get(DocumentStyle.LIST_TYPE));
			}
		}
		String c = numbered ? "#" : "*";
		int len = lists.size();
		String s = "";
		for (int i=0 ; i<len ; i++) {
			s += c;
		}
		lists.add(s);
		curList = s;
	}
	protected void doOpenListItem() throws IOException {
		writer.write(curList + " ");
	}
	protected void doCloseListItem() throws IOException {
		writer.write("\n");
	}
	protected void doCloseList() throws IOException {
		writer.write("\n");
		lists.pop();
		curList = (String)lists.lastElement();
	}
}
