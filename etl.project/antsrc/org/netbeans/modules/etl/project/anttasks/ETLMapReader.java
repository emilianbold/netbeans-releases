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
package org.netbeans.modules.etl.project.anttasks;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Utility class which reads a etlmap.xml and returns a list of etlmapEntry
 * 
 */
public class ETLMapReader {

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

	public static List<ETLMapEntry> parse(String etlmapfile) throws Exception {

		List<ETLMapEntry> etlmapEntryList = new ArrayList<ETLMapEntry>();

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(etlmapfile);
		Element elem = doc.getDocumentElement();
		NodeList etlmaps = elem.getElementsByTagName(ETLMapEntry.ETLMAP_TAG);

		for (int i = 0; i < etlmaps.getLength(); i++) {
			Node n = etlmaps.item(i);
			NamedNodeMap attrMap = n.getAttributes();
			String partnerlink = attrMap.getNamedItem(ETLMapEntry.PARTNERLINK_TAG).getNodeValue();
			String portType = attrMap.getNamedItem(ETLMapEntry.PORTTYPE_TAG).getNodeValue();
			String operation = attrMap.getNamedItem(ETLMapEntry.OPERATION_TAG).getNodeValue();
			String file = attrMap.getNamedItem(ETLMapEntry.FILE_TAG).getNodeValue();
			String type = attrMap.getNamedItem(ETLMapEntry.TYPE_TAG).getNodeValue();

			ETLMapEntry e = new ETLMapEntry(partnerlink, portType, operation, file, type);
			etlmapEntryList.add(e);

		}

		return etlmapEntryList;

	}
	
	public static void main(String[] args) {
		try {
			List<ETLMapEntry> l =  ETLMapReader.parse("test/etlmap.xml");
			l.get(0).getPortType();
			l.get(0).getPartnerLink();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
