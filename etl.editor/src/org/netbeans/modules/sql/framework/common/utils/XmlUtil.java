/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Logger;
import com.sun.etl.utils.StringUtil;

/**
 * Static class that contains utility methods for XML
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class XmlUtil {

    /** XML_SUBSTITUTES is a list of character substitutes for XML_ILLEGALS. */
    public static final String[] XML_ALLOWABLES = { "&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};

    /** XML_ILLEGALS is a list of character strings not parseable by XML. */
    public static final String[] XML_ILLEGALS = { "&", "\"", "'", "<", ">"};

    /** HTML_SUBSTITUTES is a list of character substitutes for XML_ILLEGALS. */
    public static final String[] HTML_ALLOWABLES = { "&amp;", "&quot;", "&lt;", "&gt;"};

    /** HTML_ILLEGALS is a list of character strings not parseable by XML. */
    public static final String[] HTML_ILLEGALS = { "&", "\"", "<", ">"};


    /** Runtime context for this class. */
    private static final String LOG_CATEGORY = XmlUtil.class.getName();

    private static Map xslDocumentMap = new HashMap();

    private static String ETL_COLLAB_FOLDER = getEtlCollabFolder();
    
    private static final synchronized String getEtlCollabFolder() {
        String nbUsrDir = System.getProperty("netbeans.user");
        if ((nbUsrDir == null) || ("".equals(nbUsrDir))){
            nbUsrDir = ".." + File.separator + "usrdir" ; 
        }
        return nbUsrDir + File.separator + "eTL"+ File.separator +"collab" + File.separator ;            
    } 
    
    /**
     * Writes given XML string to file with given filename.
     *
     * @param fileName name of file to receive XML output.
     * @param xmlString XML content to write out.
     */
    public static void dumpXMLString(String fileName, String xmlString) {
        if (!Logger.isDebugEnabled(LOG_CATEGORY)) {
            return;
        }

        try {
            File file = new File(ETL_COLLAB_FOLDER + fileName);
            file.getParentFile().mkdirs();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(xmlString);
            out.close();
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * The escapeXML method is used to replace illegal xml characters with their
     * acceptable equivalents.
     *
     * @param string String requiring replacement of characters.
     * @return String with illegal characters translated.
     */
    public static String escapeXML(String string) {
        return StringUtil.replaceInString(string, XML_ILLEGALS, XML_ALLOWABLES);
    }

    /**
     * The escapeXML method is used to replace illegal xml characters with their
     * acceptable equivalents.
     *
     * @param string String requiring replacement of characters.
     * @return String with illegal characters translated.
     */
    public static String escapeHTML(String string) {
        return StringUtil.replaceInString(string, HTML_ILLEGALS, HTML_ALLOWABLES);
    }


    /**
     * Extracts attribute, if any, with given name from given DOM element.
     *
     * @param element DOM element in which to locate attribute
     * @param attrName name of attribute to extract
     * @param nullIfEmptyString indicates whether to return null if attribute is not found
     *        or returns as empty string
     * @return attribute value as String. If parsing the element for attrName results in
     *         an empty String (or if either element or attrName are null), then return
     *         null if nullIfEmptyString is true, else return an empty String.
     */
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

    /**
     * Reads in String XML content from given BufferedReader and converts it to a DOM
     * Element for parsing.
     *
     * @param reader BufferedReader supplying String XML content
     * @return XML content as a DOM element
     */
    public static Element loadXMLFile(BufferedReader reader) {
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;
        Element modelElement = null;
        if (reader == null) {
            Logger.print(Logger.ERROR, LOG_CATEGORY, "Invalid stream, nothing to load...");
        }

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(reader));

            modelElement = doc.getDocumentElement();
        } catch (Exception e) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, null, "Could not load XML: " + reader, e);
        }
        return modelElement;
    }


    /**
     * Reads in String XML content from given reader and converts it to a DOM Element for
     * parsing.
     *
     * @param String supplying XML content
     * @return XML content as a DOM element
     */
    public static Element loadXMLString(String xmlString) {
        return loadXMLFile(new BufferedReader(new StringReader(xmlString)));
    }


    /**
     * Reads in String XML content from given reader and converts it to a DOM Element for
     * parsing.
     *
     * @param aReader Reader supplying String XML content
     * @return XML content as a DOM element
     */
    public static Element loadXMLFile(Reader aReader) {
        return loadXMLFile(new BufferedReader(aReader));
    }

    /**
     * Reads in String XML content from a file with the given name and converts it to a
     * DOM Element for parsing.
     *
     * @param fileName name of file containing XML content
     * @return XML content as a DOM element
     */
    public static Element loadXMLFile(String fileName) {
        return loadXMLFile(fileName, null);
    }

    /**
     * Reads in String XML content from a file with the given name and converts it to a
     * DOM Element for parsing.
     *
     * @param fileName name of file containing XML content
     * @param classLoader ClassLoader to use in resolving <code>fileName</code>
     * @return XML content as a DOM element
     */
    public static Element loadXMLFile(String fileName, ClassLoader classLoader) {
        InputStream istream = null;
        Element element = null;

        if (classLoader == null) {
            classLoader = XmlUtil.class.getClassLoader();
        }

        if (StringUtil.isNullString(fileName)) {
            Logger.print(Logger.ERROR, LOG_CATEGORY, "Invalid file name, nothing to load...");
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
                    Logger.print(Logger.ERROR, LOG_CATEGORY, "configure", "configure ERROR: Can't find file in classpath: " + fileName);
                    return null;
                }
            }

            element = loadXMLFile(new BufferedReader(new InputStreamReader(istream)));
            if (element == null) {
                throw new BaseException(LOG_CATEGORY, fileName + " is empty");
            }
        } catch (Exception e) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, null, "Could not load " + fileName, e);
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

    public static Element transform(URL xslFileUrl, Element sourceElem) throws BaseException {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();

            if (tFactory.getFeature(DOMSource.FEATURE) && tFactory.getFeature(DOMResult.FEATURE)) {
                //Instantiate a DocumentBuilderFactory.
                DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

                // And setNamespaceAware, which is required when parsing xsl files
                dFactory.setNamespaceAware(true);

                //Use the DocumentBuilderFactory to create a DocumentBuilder.
                DocumentBuilder dBuilder = dFactory.newDocumentBuilder();

                //Use the DocumentBuilder to parse the XSL style sheet.
                Document xslDoc = (Document) xslDocumentMap.get(xslFileUrl);
                if (xslDoc == null) {
                    xslDoc = dBuilder.parse(xslFileUrl.toString());
                }

                // Use the DOM Document to define a DOMSource object.
                DOMSource xslDomSource = new DOMSource(xslDoc);

                // Set the systemId: note this is actually a URL, not a local filename
                xslDomSource.setSystemId(xslFileUrl.toString());

                // Process the style sheet DOMSource and generate a Transformer.
                Transformer transformer = tFactory.newTransformer(xslDomSource);

                // Use the DOM Document to define a DOMSource object.
                DOMSource xmlDomSource = new DOMSource(sourceElem);

                // Create an empty DOMResult for the Result.
                DOMResult domResult = new DOMResult();

                // Perform the transformation, placing the output in the DOMResult.
                transformer.transform(xmlDomSource, domResult);
                Node node = domResult.getNode();
                if (node.getNodeType() == Node.DOCUMENT_NODE) {
                    return ((Document) node).getDocumentElement();
                }
            }

        } catch (Exception th) {
            throw new BaseException("can not transform source document", th);
        }

        return null;
    }
    
    public static String toXmlString(Element element) {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write(element, buffer, 0);
        return buffer.toString();
    }
    
    private static void write(Element elem, StringBuilder buf, int indentLevel) {
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

    private static void write(NodeList nodes, StringBuilder buf, int indentLevel) {
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

    /**
     * @param attr
     * @param buf
     */
    private static void write(Attr attr, StringBuilder buf) {
        buf.append(" ").append(attr.getName()).append("=\"");
        String value = attr.getValue();
        buf.append((value != null) ? XmlUtil.escapeXML(attr.getValue()) : "").append("\"");
    }
}

