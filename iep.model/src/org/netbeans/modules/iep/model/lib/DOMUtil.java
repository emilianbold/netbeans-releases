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

package org.netbeans.modules.iep.model.lib;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.xerces.dom.*;
//import org.apache.xerces.parsers.*;
//import org.apache.xml.serialize.*; 

//import org.apache.xpath.XPathAPI; (xalan-2.3.1.jar)

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.*;
import org.xml.sax.InputSource;

/**
 * Description of the Class
 *
 * @author       Bing Lu
 * @created      January 27, 2003
 */
public class DOMUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(DOMUtil.class.getName());

    /**
     * Gets the childNodeListByType attribute of the DOMUtil class
     *
     * @param node  Description of the Parameter
     * @param type  Description of the Parameter
     * @return      The childNodesByType value
     */
    public static List getChildNodeListByType(Node node, int type) {

        List list = new ArrayList();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == type) {
                list.add(child);
            }
        }

        return list;
    }

    /**
     * Gets the text attribute of the DOMUtil class
     *
     * @param node  Description of the Parameter
     * @return      The text value
     */
    public static String getText(Node node) {

        StringBuffer buf = new StringBuffer();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.TEXT_NODE) {
                buf.append(child.getNodeValue());
            }
        }

        return buf.toString();
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocument(boolean namespaceAware)
        throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        return document;
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param uri             Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocument(boolean namespaceAware, URI uri)
        throws Exception {
        return createDocument(namespaceAware, new InputSource(uri.toString()));
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param file            Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocument(boolean namespaceAware, File file)
             throws Exception {
        return createDocument(namespaceAware, new InputSource(
                new FileReader(file)));
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param source          Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocument(boolean namespaceAware,
            InputSource source)
             throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        //factory.setValidating();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(source);

        document.normalize();

        return document;
    }

    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param xml             Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    public static Document createDocumentFromXML(boolean namespaceAware,
            String xml)
             throws Exception {
        return createDocument(namespaceAware,
                new InputSource(new StringReader(xml)));
    }

    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param xpath          Description of the Parameter
     * @return               Description of the Return Value
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static NodeList selectNodeList(Node node, String xpath)
             throws Exception {
        return XPathAPI.selectNodeList(node, xpath);
    }
*/
    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param xpath          Description of the Parameter
     * @return               Description of the Return Value
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static List selectNodeListText(Node node, String xpath)
             throws Exception {

        List list = new ArrayList();

        NodeList nl = selectNodeList(node, xpath);
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            list.add(getText(n));
        }

        return list;
    }
*/
    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param xpath          Description of the Parameter
     * @return               Description of the Return Value
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static Node selectSingleNode(Node node, String xpath)
             throws Exception {
        return XPathAPI.selectSingleNode(node, xpath);
    }
*/
    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param xpath          Description of the Parameter
     * @return               Description of the Return Value
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static String selectSingleNodeText(Node node, String xpath)
             throws Exception {

        String text = null;

        Node n = selectSingleNode(node, xpath);
        if (n != null) {
            text = getText(n);
        }

        return text;
    }
*/
    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param name           Description of the Parameter
     * @return               Description of the Return Value
     */             
    public static String getAttribute(Node node, String name) {
        String text = null;

        NamedNodeMap map = node.getAttributes();
        Node attr = map.getNamedItem(name);

        if (attr != null) {
            text = attr.getNodeValue();
        }
        
        return text;
    }
             
    /**
     * Description of the Method
     *
     * @param node  Description of the Parameter
     * @return      Description of the Return Value
     */
    // UTF-8
    public static String toXML(
        Node node, String encoding, boolean omitXMLDeclaration) {
        String ret = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Transformer trans =
                TransformerFactory.newInstance().newTransformer();

            trans.setOutputProperty(OutputKeys.ENCODING, encoding);
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            if (omitXMLDeclaration) {
                trans.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes");
            } else {
                trans.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "no");
            }
            trans.transform(new DOMSource(node), new StreamResult(baos));
            ret = baos.toString(encoding);
            //mLog.debug("ret: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(e.getMessage());
        }
        return ret;
    }
/*
    public static void toXML(Node node) {
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: {
                System.out.println("<?xml version=\"1.0\" ?>");
                printDOM(((Document)node).getDocumentElement());
                break;
            }
            case Node.ELEMENT_NODE: {
                System.out.print("<");
                System.out.print(node.getNodeName());
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    System.out.print(" " + attr.getNodeName().trim() +
                     "=\"" + attr.getNodeValue().trim() +
                     "\"");
                }
                System.out.println(">");

                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++)
                        printDOM(children.item(i));
                }
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                System.out.print("&");
                System.out.print(node.getNodeName().trim());
                System.out.print(";");
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                System.out.print("");
                break;
            }
            case Node.TEXT_NODE: {
                System.out.print(node.getNodeValue().trim());
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                System.out.print("");   
                break; 
            }     
        }
    }
*/    
/*
    public static String toXML(
        Document doc, String encoding, boolean emitXMLDeclaration)
        throws Exception {
        OutputFormat format = new OutputFormat(doc, encoding, true);
        //format.setLineSeparator("\r\n");
        format.setIndent(4);
        format.setLineWidth(80);
        format.setPreserveSpace(false);
        StringWriter writer = new StringWriter();
        XMLSerializer serializer =
            new XMLSerializer(writer, format);
        serializer.serialize(doc);
        return writer.toString();
    }
*/
/*
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(
            // Ignore DOCTYPE entity
            new org.xml.sax.EntityResolver() {
                public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                    mLog.debug(publicId + " " + systemId);
                    java.io.StringReader reader = new java.io.StringReader("");
                    return new org.xml.sax.InputSource(reader);
                }
            });
*/    
    
    /**
     * Description of the Method
     *
     * @param node           Description of the Parameter
     * @param xpath          Description of the Parameter
     * @return               Description of the Return Value
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static Node buildPath(Node node, String xpath)
             throws Exception {
        Document document = null;
        if (node instanceof Document) {
            document = (Document) node;
        } else {
            document = node.getOwnerDocument();
        }

        if (xpath.charAt(0) == '/') {
            node = document;

            xpath = xpath.substring(1);
        }

        int i = xpath.indexOf('/');
        String nodeName = (i < 0)
                ? xpath : xpath.substring(0, i);

        //mLog.debug("node: " + node + " nodeName: " + nodeName + " xpath: " + xpath);

        Node child = selectSingleNode(node, nodeName);
        if (child == null) {
            child = document.createElement(nodeName);

            node.appendChild(child);
        }

        if (i < 0) {
            return child;
        } else {
            return buildPath(
                    child, xpath.substring(i + 1));
        }
    }
*/
    public static String escapeText(String value) {
        StringBuffer ret = new StringBuffer();
        int len = (value != null) ?
            value.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '<' : {
                    ret.append("&lt;");
                    break;
                }
                case '>' : {
                    ret.append("&gt;");
                    break;
                }
                case '&' : {
                    ret.append("&amp;");
                    break;
                }
                case '"' : {
                    ret.append("&quot;");
                    break;
                }
                case '\'' : {
                    ret.append("&apos;");
                    break;
                }
                case '\r' :
                case '\n' : {
                    ret.append("&#");
                    ret.append(Integer.toString(ch));
                    ret.append(';');
                    break;
                }
                // else, default append char
                default : {
                    ret.append(ch);
                    break;
                }
            }
        }
        return ret.toString();
    }    
    
    /**
     * The main program for the DOMUtil class
     *
     * @param args           The command line arguments
     * @exception Exception  Description of the Exception
     */
/***XPathAPI    
    public static void main(String[] args)
             throws Exception {
        String[] xpaths = new String[]{
                "/Person", "/Person/FirstName", "/Person/Address", "/Person/Address/AddressLine1"};

        Document document = createDocument(true);
        for (int i = 0; i < xpaths.length; i++) {
            buildPath(document, xpaths[i]);
            mLog.info(toXML(document, "UTF-8", false));
        }
    }
*/
}
