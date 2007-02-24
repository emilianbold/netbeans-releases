/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.support;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Owner
 */

public class IndentingParser {

	/** Creates a new instance of IndentingParser */
	public IndentingParser() {
	}

	private String displayStrings[] = new String[1000];

	private int numberDisplayLines = 0;

	public void displayDocument(InputStream in)
	{
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(in);
			display(document, "");
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}



	public void display(Node node, String indent)
	{
		if (node == null) {
			return;
		}
		int type = node.getNodeType();
		switch (type) {
			case Node.DOCUMENT_NODE: {
				displayStrings[numberDisplayLines] = indent;
				displayStrings[numberDisplayLines] +=
						"<?xml version=\"1.0\" encoding=\""+
						"UTF-8" + "\"?>";
				numberDisplayLines++;
				display(((Document)node).getDocumentElement(), "");
				break;
			}

			case Node.ELEMENT_NODE: {
				displayStrings[numberDisplayLines] = indent;
				displayStrings[numberDisplayLines] += "<";
				displayStrings[numberDisplayLines] += node.getNodeName();
				int length = (node.getAttributes() != null) ?
					node.getAttributes().getLength() : 0;
				Attr attributes[] = new Attr[length];
				for (int loopIndex = 0; loopIndex < length; loopIndex++) {
					attributes[loopIndex] =
							(Attr)node.getAttributes().item(loopIndex);
				}

				for (int loopIndex = 0; loopIndex < attributes.length;
				loopIndex++) {
					Attr attribute = attributes[loopIndex];
					displayStrings[numberDisplayLines] += " ";
					displayStrings[numberDisplayLines] +=
							attribute.getNodeName();
					displayStrings[numberDisplayLines] += "=\"";
					displayStrings[numberDisplayLines] +=
							attribute.getNodeValue();
					displayStrings[numberDisplayLines] += "\"";
				}
				displayStrings[numberDisplayLines] += ">";
				numberDisplayLines++;
				NodeList childNodes = node.getChildNodes();
				if (childNodes != null) {
					length = childNodes.getLength();
					indent += "    ";
					for (int loopIndex = 0; loopIndex < length; loopIndex++ ) {
						display(childNodes.item(loopIndex), indent);
					}
				}
				break;
			}
			case Node.CDATA_SECTION_NODE: {
				displayStrings[numberDisplayLines] = indent;
				displayStrings[numberDisplayLines] += "<![CDATA[";
				displayStrings[numberDisplayLines] += node.getNodeValue();
				displayStrings[numberDisplayLines] += "]]>";
				numberDisplayLines++;
				break;
			}
			case Node.TEXT_NODE: {
				displayStrings[numberDisplayLines] = indent;
				String newText = node.getNodeValue().trim();
				if(newText.indexOf("\n") < 0 && newText.length() > 0) {
					displayStrings[numberDisplayLines] += newText;
					numberDisplayLines++;
				}
				break;
			}
			case Node.PROCESSING_INSTRUCTION_NODE: {
				displayStrings[numberDisplayLines] = indent;
				displayStrings[numberDisplayLines] += "<?";
				displayStrings[numberDisplayLines] += node.getNodeName();
				String text = node.getNodeValue();
				if (text != null && text.length() > 0) {
					displayStrings[numberDisplayLines] += text;
				}
				displayStrings[numberDisplayLines] += "?>";
				numberDisplayLines++;
				break;
			}
		}

		if (type == Node.ELEMENT_NODE) {
			displayStrings[numberDisplayLines] = indent.substring(0,
					indent.length() - 4);
			displayStrings[numberDisplayLines] += "</";
			displayStrings[numberDisplayLines] += node.getNodeName();
			displayStrings[numberDisplayLines] += ">";
			numberDisplayLines++;
			indent += "    ";
		}
	}

	public void writeDocument(OutputStream out){
		try{
			BufferedOutputStream bw = new BufferedOutputStream(out);

			for(int loopIndex = 0; loopIndex < numberDisplayLines; loopIndex++){
				String str=displayStrings[loopIndex];
				for(int j=0;j<str.length();j++)
					bw.write(str.charAt(j));
				bw.write('\n');
			}
			bw.flush();
			bw.close();
		}catch(java.io.FileNotFoundException fnf){
		}catch(java.io.IOException fnf){
		}
	}

	public void parse(InputStream in, OutputStream out){
		IndentingParser idp = new IndentingParser();
		idp.displayDocument(in);
		idp.writeDocument(out);
	}
}