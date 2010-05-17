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
package org.netbeans.modules.edm.project.anttasks;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;

/**
 *
 */

public class JBIFileWriter {

	private String mJbiDescriptorFile;

	private String etlMapFile;

	// keyed by prefix
	private Map<String, String> prefixTable = new HashMap<String, String>();

	// keyed by name space
	private Map<String, String> nsTable = new HashMap<String, String>();

	private FileOutputStream fos = null;

	public JBIFileWriter(String mJbiDescriptorFile, String etlMapFile) {
		this.mJbiDescriptorFile = mJbiDescriptorFile;
		this.etlMapFile = etlMapFile;
	}

	public void writeJBI() {

		List<EDMMapEntry> etlEntryList = null;
		try {
			etlEntryList = EDMMapReader.parse(etlMapFile);
			populatePrefixAndNamespaceTable(etlEntryList);
			generateJBI(etlEntryList);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * @param etlEntryList
	 * @throws BuildException
	 */
	private void populatePrefixAndNamespaceTable(List<EDMMapEntry> etlEntryList)
			throws BuildException {
		try {

			// Populate prefixTable
			int nsIndex = 1;
			for (int i = 0, I = etlEntryList.size(); i < I; i++) {
				EDMMapEntry entry = etlEntryList.get(i);
				String ns = entry.getPartnerLink().getNamespaceURI();
				if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
					nsTable.put(ns, "ns" + nsIndex);
					prefixTable.put("ns" + nsIndex, ns);
					nsIndex++;
				}

				ns = entry.getPortType().getNamespaceURI();
				if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
					nsTable.put(ns, "ns" + nsIndex);
					prefixTable.put("ns" + nsIndex, ns);
					nsIndex++;
				}

				if (entry.getType() != EDMMapEntry.REQUEST_REPLY_SERVICE) {
					ns = entry.getPartnerLink().getNamespaceURI();
					if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
						nsTable.put(ns, "ns" + nsIndex);
						prefixTable.put("ns" + nsIndex, ns);
						nsIndex++;
					}

					ns = entry.getPortType().getNamespaceURI();
					if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
						nsTable.put(ns, "ns" + nsIndex);
						prefixTable.put("ns" + nsIndex, ns);
						nsIndex++;
					}
				}
			}

		} catch (Exception e) {
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * @param etlEntryList
	 * @throws Exception
	 */
	private void generateJBI(List<EDMMapEntry> etlEntryList) throws Exception {
		// Generate jbi.xml
		// <?xml version='1.0'?>
		// <jbi version="1.0"
		// xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		// xmlns="http://java.sun.com/xml/ns/jbi"
		// xsi:schemaLocation="http://java.sun.com/xml/ns/jbi jbi.xsd"
		// xmlns:ns0=${ns1} ... xmlns:nsN=${nsN} >
		// <services binding-component="false">
		// <provides interface-name=port-type service-name=partner-link
		// endpoint-name=role-name/>
		// <consumes interface-name=port-type service-name=partner-link
		// endpoint-name=role-name link-type="standard"/>
		// </services>
		// </jbi>
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("<!--start of generated code -->\n");
			sb.append("<jbi version=\"1.0\"\n");
			sb.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
			sb.append("        xmlns=\"http://java.sun.com/xml/ns/jbi\"\n");
			sb.append("        xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi jbi.xsd\"\n");
			for (int i = 0, I = nsTable.size(); i < I; i++) {
				String ns = "ns" + (i + 1);
				sb.append("        xmlns:" + ns + "=\"" + prefixTable.get(ns) + "\"");
				if (i < I - 1) {
					sb.append("\n");
				}
			}
			sb.append(">\n");
			sb.append("    <services binding-component=\"false\">\n");
			// Generate all <provides> first
			for (int i = 0, I = etlEntryList.size(); i < I; i++) {
				EDMMapEntry xme = (EDMMapEntry) etlEntryList.get(i);
				sb.append("        <provides interface-name=\""
						+ getDottedQName(xme.getPortType(), nsTable));
				sb.append("\" service-name=\"" + getDottedQName(xme.getPartnerLink(), nsTable));
				sb.append("\" endpoint-name=\"" + xme.getRoleName());
				sb.append("\"/>\n");
			}
			// Generate all <consumes> second
			for (int i = 0, I = etlEntryList.size(); i < I; i++) {
				EDMMapEntry xme = (EDMMapEntry) etlEntryList.get(i);
				if (!xme.getType().equals(EDMMapEntry.REQUEST_REPLY_SERVICE)) {
					sb.append("        <consumes interface-name=\""
							+ getDottedQName(xme.getPortType(), nsTable));
					sb.append("\" service-name=\"" + getDottedQName(xme.getPartnerLink(), nsTable));
					sb.append("\" endpoint-name=\"" + xme.getRoleName());
					sb.append("\" link-type=\"standard\"/>\n");
				}
			}
			sb.append("    </services>\n");
			sb.append(" </jbi>\n");
			sb.append("<!--end of generated code -->\n");

			String content = sb.toString();
			fos = new FileOutputStream(mJbiDescriptorFile);
			FileUtil.copy(content.getBytes("UTF-8"), fos);
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private static String getDottedQName(QName qn, Map nsTable) {
		String ns = qn.getNamespaceURI();
		String prefix = (String) nsTable.get(ns);
		if (prefix == null) {
			return qn.getLocalPart();
		}
		return prefix + ":" + qn.getLocalPart();
	}

	public static void main(String[] args) {
		JBIFileWriter fw = new JBIFileWriter("test/jbi.xml", "test/edmmap.xml");
		fw.writeJBI();

	}

}
