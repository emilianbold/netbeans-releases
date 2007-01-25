package org.netbeans.modules.sql.project.anttasks;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SQLMapReader {

	/*
	 * 
	 * 
	 * sample etlmap.xml
	 * 
	 * <?xml version="1.0" encoding="UTF-8"?> <etlmap
	 * xmlns:tns="http://com.sun.com/etl/etlengine"
	 * targetNamespace="http://com.sun.com/etl/etlengine" > <etl
	 * partnerLink="{http://com.sun.com/etl/etlengine}Client2ETELLink"
	 * portType="{http://com.sun.com/etl/etlengine}etlPortType"
	 * operation="execute" file="etl-engine.xml" type="requestReplyService"/>
	 * </etlmap>
	 * 
	 */

	public static List parse(String sqlmapfile) throws Exception {

		List etlmapEntryList = new ArrayList();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sqlmapfile);
		Element elem = doc.getDocumentElement();
		NodeList etlmaps = elem.getElementsByTagName(SQLMapEntry.SQLMAP_TAG);

		for (int i = 0; i < etlmaps.getLength(); i++) {
			Node n = etlmaps.item(i);
			NamedNodeMap attrMap = n.getAttributes();
			String partnerlink = attrMap.getNamedItem(SQLMapEntry.PARTNERLINK_TAG).getNodeValue();
			String portType = attrMap.getNamedItem(SQLMapEntry.PORTTYPE_TAG).getNodeValue();
			String operation = attrMap.getNamedItem(SQLMapEntry.OPERATION_TAG).getNodeValue();
			String sqlfile = attrMap.getNamedItem(SQLMapEntry.SQL_FILE_TAG).getNodeValue();
            String wsdlfile = attrMap.getNamedItem(SQLMapEntry.WSDL_FILE_TAG).getNodeValue();
			String type = attrMap.getNamedItem(SQLMapEntry.TYPE_TAG).getNodeValue();

			SQLMapEntry e = new SQLMapEntry(partnerlink, portType, operation, sqlfile, wsdlfile, type);
			etlmapEntryList.add(e);

		}

		return etlmapEntryList;

	}
	
	public static void main(String[] args) {
		try {
			List l =  SQLMapReader.parse("test/sqlmap.xml");
            SQLMapEntry entry = (SQLMapEntry) l.get(0);
			entry.getPortType();
			entry.getPartnerLink();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
