/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */
package org.netbeans.modules.edm.editor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

/**
 * Static class that contains utility methods for XML
 *
 * @author Ahimanikya Satapathy
 */
public class XmlUtil {

    public static final String[] XML_ALLOWABLES = {"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};
    public static final String[] XML_ILLEGALS = {"&", "\"", "'", "<", ">"};
    public static final String[] HTML_ALLOWABLES = {"&amp;", "&quot;", "&lt;", "&gt;"};
    public static final String[] HTML_ILLEGALS = {"&", "\"", "<", ">"};
    private static final String LOG_CATEGORY = XmlUtil.class.getName();

    public static String escapeXML(String string) {
        return StringUtil.replaceInString(string, XML_ILLEGALS, XML_ALLOWABLES);
    }

    public static String escapeHTML(String string) {
        return StringUtil.replaceInString(string, HTML_ILLEGALS, HTML_ALLOWABLES);
    }

    public static String getAttributeFrom(Element element, String attrName, boolean nullIfEmptyString) {
        if (element == null || attrName == null) {
            return (nullIfEmptyString) ? null : "";
        }

        String val = element.getAttribute(attrName);
        if ("".equals(val) && nullIfEmptyString) {
            val = null;
        }

        return val;
    }

    public static Element loadXMLFile(BufferedReader reader) {
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;
        Element modelElement = null;
        if (reader == null) {
            Logger.global.log(Level.FINE, NbBundle.getMessage(XmlUtil.class, "LOG.FINE_Invalid_stream"));
        }

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(reader));

            modelElement = doc.getDocumentElement();
        } catch (Exception e) {
            Logger.global.log(Level.FINE, NbBundle.getMessage(XmlUtil.class, "LOG.FINE_Could_not_load_XML") + reader, e);
        }
        return modelElement;
    }

    public static Element loadXMLFile(Reader aReader) {
        return loadXMLFile(new BufferedReader(aReader));
    }

    public static Element loadXMLFile(String fileName) {
        return loadXMLFile(fileName, null);
    }

    public static Element loadXMLFile(String fileName, ClassLoader classLoader) {
        InputStream istream = null;
        Element element = null;

        if (classLoader == null) {
            classLoader = XmlUtil.class.getClassLoader();
        }

        if (StringUtil.isNullString(fileName)) {
            Logger.global.log(Level.FINE, NbBundle.getMessage(XmlUtil.class, "LOG.FINE_Invalid_file_name"));
        }

        try {
            istream = classLoader.getResourceAsStream(fileName);
            if (istream == null) {
                File configFile = new File(fileName);

                if (configFile.exists()) {
                    fileName = configFile.getAbsolutePath();
                    istream = new FileInputStream(fileName);
                }

                if (istream == null) {
                    Logger.global.log(Level.FINE, "configure", NbBundle.getMessage(XmlUtil.class, "ERROR_Can't_find_file_in_classpath") + fileName);
                    return null;
                }
            }

            element = loadXMLFile(new BufferedReader(new InputStreamReader(istream)));
            if (element == null) {
                throw new EDMException(LOG_CATEGORY, fileName + NbBundle.getMessage(XmlUtil.class, "ERROR_is_empty"));
            }
        } catch (Exception e) {
            Logger.global.log(Level.FINE, NbBundle.getMessage(XmlUtil.class, "ERROR_Could_not_load") + fileName, e);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                    // ignore this exception
                }
            }
        }
        return element;
    }

    public static String toXmlString(Element element) {
        StringBuffer buffer = new StringBuffer(200);
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write(element, buffer, 0);
        return buffer.toString();
    }

    private static void write(Element elem, StringBuffer buf, int indentLevel) {
        final String elementName = elem.getNodeName();

        buf.append("<");
        buf.append(elementName);

        NamedNodeMap attrMap = elem.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            write((Attr) attrMap.item(i), buf);
        }

        if (elem.hasChildNodes()) {
            buf.append(">");

            NodeList childNodes = elem.getChildNodes();
            write(childNodes, buf, indentLevel + 1);

            buf.append("</").append(elementName).append(">");
        } else {
            buf.append(" />");
        }
    }

    private static void write(NodeList nodes, StringBuffer buf, int indentLevel) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node aNode = nodes.item(i);
            if (aNode != null) {
                short nodeType = aNode.getNodeType();
                switch (nodeType) {
                    case Node.CDATA_SECTION_NODE:
                        CharacterData cdata = (CharacterData) aNode;
                        buf.append("<![CDATA[").append(cdata.getData()).append("]]>");
                        break;

                    case Node.TEXT_NODE:
                        Text myText = (Text) aNode;
                        buf.append(XmlUtil.escapeXML(myText.getData()));
                        break;

                    case Node.ELEMENT_NODE:
                        write((Element) aNode, buf, indentLevel);
                        break;
                }
            }
        }
    }

    private static void write(Attr attr, StringBuffer buf) {
        buf.append(" ").append(attr.getName()).append("=\"");
        String value = attr.getValue();
        buf.append((value != null) ? XmlUtil.escapeXML(attr.getValue()) : "").append("\"");
    }
}
