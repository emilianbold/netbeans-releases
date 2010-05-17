/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
