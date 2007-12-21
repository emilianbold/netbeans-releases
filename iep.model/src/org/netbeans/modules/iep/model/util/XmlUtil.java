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

/*
 * @(#)XmlUtil.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package org.netbeans.modules.iep.model.util;

import java.io.*;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import org.xml.sax.InputSource;

/**
 * Description of the Class
 *
 * 
 */
public class XmlUtil {
      
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

        factory.setNamespaceAware(namespaceAware);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        return document;
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
     * Gets the text attribute of the DOMUtil class
     *
     * @param node  Description of the Parameter
     * @return      The text value
     */
    public static String getText(Node node) {
        StringBuffer buf = new StringBuffer();
        if (node.getNodeType() == Node.TEXT_NODE) {
            buf.append(node.getNodeValue());
        } else {
            NodeList children = node.getChildNodes();
            for (int i = 0, I = children.getLength(); i < I; i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    buf.append(child.getNodeValue());
                }
            }
        }
        return buf.toString().trim();
    }

    /**
     * Description of the Method
     *
     * @param node  Description of the Parameter
     * @return      Description of the Return Value
     */
    // UTF-8
    public static String toXml(Node node, String encoding, boolean omitXMLDeclaration) {
        String ret = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.ENCODING, encoding);
//            trans.setOutputProperty(OutputKeys.INDENT, "yes");
//            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration? "yes":"no");
            trans.transform(new DOMSource(node), new StreamResult(baos));
            ret = baos.toString(encoding);
            //mLogger.debug("ret: " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    //TODO
    public static Element buildFault(Exception e) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public static NodeList newSingleNodeList(Node node) {
        return new DefaultNodeList(node);
    }
    
    static class DefaultNodeList implements NodeList {
        private Node mNode;
        
        DefaultNodeList(Node node) {
            this.mNode = node;
        }
        public int getLength() {
            return mNode != null? 1 : 0;
        }

        public Node item(int index) {
            if(index == 0) {
                return mNode;
            }
            
            throw new IllegalArgumentException("Index out of bound "+ index);
        }

    }
}
