/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.completion.entries;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Martin Fousek
 */
public class CodeCompletionEntries {

    public CodeCompletionEntries() {
    }

    protected static Collection<EntryMetadata> readAllCodeCompletionEntriesFromXML(InputStream inputStream, String completionType) throws IOException, ParserConfigurationException, SAXException {
        Collection<EntryMetadata> ccEntries = new ArrayList<EntryMetadata>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList allEntriesList = doc.getElementsByTagName("entry");
        for (int i = 0; i < allEntriesList.getLength(); i++) {
            Node ccNode = allEntriesList.item(i);

            if (ccNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) ccNode;
                String desc = elem.getElementsByTagName("description").item(0).getTextContent();
                String url = elem.getElementsByTagName("url").item(0).getTextContent();
                String help = "";
                NodeList attributes = elem.getElementsByTagName("attributes");
                if (completionType.equals("built-in-functions")) {
                    help = generateHelpForBuiltInFunctions(desc, attributes);
                } else {
                    help = generateHelpForVariableModifiers(desc, attributes);
                }
                ccEntries.add(new CCDataBuiltInFunction(elem.getAttribute("name"), help, url));
            }
        }

        return ccEntries;
    }

    private static String generateHelpForBuiltInFunctions(String desc, NodeList attributesRoot) {
        Element parent = (Element) (attributesRoot.item(0));
        if (parent != null) {
            String help = desc + "<br><br><table border=1>"
                    + "<tr style=\"font-weight:bold\"><td>Attribute name</td><td>Type</td><td>Required</td><td>Default</td><td>Description</td></tr>";
            NodeList attributes = parent.getChildNodes();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) attributes.item(i);
                    help += "<tr><td>" + attribute.getAttribute("name") + "</td>";
                    help += getRestOfAttributeParams(attribute);
                    help += "</tr>";
                }
            }
            help += "</table>";
            return help;
        } else {
            return desc;
        }
    }

    private static String generateHelpForVariableModifiers(String desc, NodeList attributesRoot) {
        Element parent = (Element) (attributesRoot.item(0));
        if (parent != null) {
            String help = desc + "<br><br><table border=1>"
                    + "<tr style=\"font-weight:bold\"><td>Parameter Position</td><td>Type</td><td>Required</td><td>Default</td><td>Description</td></tr>";
            NodeList attributes = parent.getChildNodes();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) attributes.item(i);
                    help += "<tr><td>" + attribute.getAttribute("position") + "</td>";
                    help += getRestOfAttributeParams(attribute);
                    help += "</tr>";
                }
            }
            help += "</table>";
            return help;
        } else {
            return desc;
        }
    }

    private static String getRestOfAttributeParams(Element attributeParams) {
        String help = "";
        help += "<td>" + attributeParams.getElementsByTagName("type").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("required").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("default").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("description").item(0).getTextContent() + "</td>";
        return help;
    }
}
