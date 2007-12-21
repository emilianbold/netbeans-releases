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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alexander Zgursky
 */
public final class XmlUtil {
    
    private static DocumentBuilder cDocumentBuilder;
    
    /** Creates a new instance of Util */
    private XmlUtil() {
    }
    
    public static String toString(
            final Node node) {
        StringWriter writer = new StringWriter();
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        TransformerFactory factory = null;
        factory = TransformerFactory.newInstance();
        
        Transformer transformer = null;
        try {
            transformer = factory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
            return "";
        } catch (TransformerFactoryConfigurationError ex) {
            ex.printStackTrace();
            return "";
        }
        
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return "";
        }
        
        return writer.toString();
    }
    
    public static boolean isTextOnlyNode(
            final Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            final NodeList children = node.getChildNodes();
            
            if ((children.getLength() == 1) && (children.item(0).
                    getNodeType() == Node.TEXT_NODE)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Element parseXmlElement(
            final String xml) {
        
        Document doc = null;
        
        if (xml != null && xml.length() > 0) {
            final InputSource is = new InputSource(new StringReader(xml));
            
            try {
                doc = getDocumentBuilder().parse(is);
            } catch (ParserConfigurationException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (IOException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (SAXException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            }
        }
        
        if (doc != null) {
            return doc.getDocumentElement();
        } else {
            return null;
        }
    }
    
    private static DocumentBuilder getDocumentBuilder(
            ) throws ParserConfigurationException {
        
        if (cDocumentBuilder == null) {
            final DocumentBuilderFactory factory = 
                    DocumentBuilderFactory.newInstance();
            
            factory.setIgnoringComments(false);
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(false);
            
            cDocumentBuilder = factory.newDocumentBuilder();
        }
        
        return cDocumentBuilder;
    }
}
