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

package org.netbeans.modules.compapp.projects.jbi.descriptor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * XML helper methods
 *
 * @author tli
 */
public class XmlUtil {
    /**
     * DOCUMENT ME!
     *
     * @param fileLocation DOCUMENT ME!
     *
     * @throws javax.xml.transform.TransformerConfigurationException DOCUMENT ME!
     * @throws javax.xml.transform.TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static void writeToFile(String fileLocation, Document document)
        throws TransformerConfigurationException, TransformerException, 
            FileNotFoundException, UnsupportedEncodingException {

        File outputFile = new File(fileLocation);
        
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        PrintWriter pw = new PrintWriter(outputFile, "UTF-8"); //USE PRINTWRITER
        StreamResult result = new StreamResult(pw);
        
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");   // NOI18N
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N
        
        // indent the output to make it more legible... 
        try {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // NOI18N
        } catch (Exception e) {
            ; // the JAXP implementation doesn't support indentation, no big deal
        }
        transformer.transform(source, result);
        pw.flush();
        pw.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws TransformerConfigurationException DOCUMENT ME!
     * @throws TransformerException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static byte[] writeToBytes(Document document)
        throws TransformerConfigurationException, TransformerException, Exception {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N

        // indent the output to make it more legible...
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");   // NOI18N
        transformer.transform(source, result);

        return bos.toByteArray();
    }


    /**
     * Description of the Method
     *
     * @param namespaceAware  Description of the Parameter
     * @param source          Description of the Parameter
     * @return                Description of the Return Value
     * @exception Exception   Description of the Exception
     */
    private static Document createDocument(boolean namespaceAware,
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
    
    /*
    public static void copyElementAttributes(Element oldElement, Element newElement) {
        NamedNodeMap attrs = oldElement.getAttributes();
        for (int k = 0; k < attrs.getLength(); k++) {
            Node attrNode = attrs.item(k);
            String name = attrNode.getNodeName();
            String value = attrNode.getNodeValue();
            newElement.setAttribute(name, value);
        }
    }
    */
    
    public static Map<String, String> getNamespaceMap(Document document) {
        Map<String, String> nsMap = new HashMap<String, String>();

        NamedNodeMap map = document.getDocumentElement().getAttributes();
        for (int j = 0; j < map.getLength(); j++) {
            Node n = map.item(j);
            String attrName = ((Attr)n).getName();
            String attrValue = ((Attr)n).getValue();
            if (attrName != null && attrValue != null) {
                if (attrName.trim().startsWith("xmlns:")) {
                    nsMap.put(attrValue, attrName.substring(6));
                }
            }
        }

        return nsMap;
    }

    public static QName getAttributeNSName(Element e, String attrName) {
        String attrValue = e.getAttribute(attrName);
        return getNSName(e, attrValue);
    }
    
    private static QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String name = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespaceURI(e, prefix), name);
        } else {
            return new QName(qname);
        }
    }
        
    public static String getNamespaceURI(Element el, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }
        prefix = prefix.trim();
        try {
            NamedNodeMap map = el.getOwnerDocument().getDocumentElement().getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String attrName = ((Attr)n).getName();
                if (attrName != null) {
                    if (attrName.trim().equals("xmlns:" + prefix)) {
                        return ((Attr)n).getValue();
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return "";
    }
    
}
