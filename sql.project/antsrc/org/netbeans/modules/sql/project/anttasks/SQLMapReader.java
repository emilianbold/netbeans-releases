/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.anttasks;

import java.io.File;
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
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().parse(sqlmapfile);
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

	public static List parse(String sqlmapfile,String mbuildDir) throws Exception {

		List etlmapEntryList = new ArrayList();
		
		//Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sqlmapfile);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = factory.newDocumentBuilder().parse(new File(mbuildDir, sqlmapfile));
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
