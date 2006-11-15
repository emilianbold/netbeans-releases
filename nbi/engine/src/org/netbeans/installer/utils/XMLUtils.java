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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class XMLUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ATTR_BEGIN  = "[@";
    private static final String ATTR_END    = "]";
    private static final String ATTR_DELIM  = "=";
    private static final String ATTRS_DELIM = " and ";
    
    public static final String XSLT_REFORMAT_URI =
            "resource:org/netbeans/installer/utils/xml/reformat.xslt";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void saveXMLDocument(Document document, File file) throws XMLException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            saveXMLDocument(document, output);            
        } catch (IOException e) {
            throw new XMLException("Cannot save XML document", e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, e);
                }
            }
        }
    }
    
    public static void saveXMLDocument(Document document, OutputStream output) throws XMLException {
        
        Source domSource = new DOMSource(document);
        Result streamResult = new StreamResult(output);
        Source xsltSource = null;
        try {
            File xslt = FileProxy.getInstance().getFile(XSLT_REFORMAT_URI);
            xsltSource = new StreamSource(xslt);
        } catch (DownloadException e) {
            LogManager.log(ErrorLevel.MESSAGE, 
                    "Cannot load XSLT file, so save XML without formatting");
            LogManager.log(ErrorLevel.MESSAGE, e);
        }
        
        try {
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = (xsltSource!=null) ?
                transformerFactory.newTransformer(xsltSource) :
                transformerFactory.newTransformer();
            
            transformer.transform(domSource, streamResult);
        } catch (TransformerConfigurationException e) {
            throw new XMLException("Cannot save XML document", e);
        } catch (TransformerException e) {
            throw new XMLException("Cannot save XML document", e);
        }
    }
    
    public static List<Node> getChildList(Node root, String... children) {
        List<Node> resultList = new LinkedList<Node>();
        
        if (root != null)  {
            if (children.length > 0) {
                resultList.add(root);
                if (children.length == 1) {
                    if (children[0].startsWith("./")) {
                        children [0] = children [0].substring("./".length());
                    }
                    children = children [0].split("/");
                }
                
                for (String child: children) {
                    resultList = getChildListFromRootList(resultList, child);
                }
            }
        }
        return resultList;
    }
    
    public static List<Element> getChildren(Element element, String... names) {
        List<Element> resultList = new LinkedList<Element>();
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                for (int j = 0; j < names.length; j++) {
                    if (node.getNodeName().equals(names[j])) {
                        resultList.add((Element) node);
                        break;
                    }
                }
            }
        }
        
        return resultList;
    }
    
    public static Element getChild(Element element, String name) {
        Element child = null;
        
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ((node instanceof Element) && node.getNodeName().equals(name)) {
                child = (Element) node;
                break;
            }
        }
        
        return child;
    }
    
    public static Node getChildNode(Node root, String... children) throws ParseException {
        List<Node> list = getChildList(root, children);
        
        if (list.size() == 0) {
            return null;
        }
        
        if (list.size() > 1 ) {
            throw new ParseException("Requested single node, returned " +
                    list.size() + " nodes");
        }
        
        return list.get(0);
    }
    
    public static String getAttribute(Node node, String name) {
        String value = null;
        
        if ((node != null) && (name != null)) {
            if (name.startsWith("./@")) {
                name = name.substring("./@".length());
            }
            
            NamedNodeMap map = node.getAttributes();
            if (map != null) {
                Node attribute = map.getNamedItem(name);
                if ((attribute != null) && (attribute.getNodeType() == Node.ATTRIBUTE_NODE)) {
                    value = attribute.getNodeValue();
                }
            }
        }
        
        return value;
    }
    
    public static String getTextContent(Node node) {
        return (node == null) ? null : node.getTextContent();
    }
    
    public static String getChildNodeTextContent(Node root, String... childs) throws ParseException {
        return getTextContent(getChildNode(root,childs));
    }
    
    public static Element addChildNode(Node parentNode, String tag, String textContent) {
        Element result = null;
        if (parentNode!=null && tag!=null) {
            
            result = parentNode.getOwnerDocument().createElement(tag);
            if (textContent!=null) {
                result.setTextContent(textContent);
            }
            parentNode.appendChild(result);
        }
        return result;
    }
    
    public static Map<String, String> loadProperties(Element element) {
        Map<String, String> properties = new HashMap<String, String>();
        
        for (Element child: XMLUtils.getChildren(element, "property")) {
            String name  = XMLUtils.getAttribute(child, "name");
            String value = XMLUtils.getTextContent(child);
            
            properties.put(name, value);
        }
        
        return properties;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static String[] getChildNamesFromString(String childname, String name) {
        String[] result = new String[] {};
        if(childname!=null) {
            if (childname.equals(name)) {
                result = new String[] { childname };
            } else if (childname.startsWith("(") && childname.endsWith(")")) {
                // several childs in round brackets separated by commas
                int len = childname.length();
                String[] names = childname.substring(1,len-1).split(",");
                int index =0;
                for (String n:names) {
                    if (name.equals(n)) {
                        index ++;
                    }
                }
                result = new String [index];
                index  = 0 ;
                for (String n:names) {
                    if (name.equals(n)) {
                        result[index] = n;
                        index ++;
                    }
                }
            }
        }
        return result;
    }
    
    private static HashMap <String,String> getAttributesFromChildName(String childname) {
        HashMap <String,String> map = new HashMap <String,String> ();
        if(childname!=null) {
            int start = childname.indexOf(ATTR_BEGIN);
            int end = childname.indexOf(ATTR_END);
            if (start!=-1 && end == (childname.length()-1 )) {
                // child with specified attribute
                String sub = childname.substring(start + ATTR_BEGIN.length(), end);
                String[] attrs = sub.split(ATTRS_DELIM);
                for (String s: attrs) {
                    String[] nameValue = s.split(ATTRS_DELIM);
                    if (nameValue.length==2) {
                        if (nameValue[1].indexOf("\"")==0 && nameValue[1].lastIndexOf("\"")==(nameValue[1].length()-1)) {
                            nameValue[1] = nameValue[1].substring(1,nameValue[1].length()-1);
                        }
                        map.put(nameValue[0],nameValue[1]);
                    }
                }
            }
        }
        return map;
    }
    
    private static boolean hasAttributes(Node childNode, HashMap <String, String> attributes) {
        int size = attributes.size();
        if (size==0) {
            return true;
        } else {
            Object [] keys = attributes.keySet().toArray();
            for (int i=0;i<size;i++) {
                if (keys[i] instanceof String) {
                    if (!getAttribute(childNode,(String)keys[i]).equals(attributes.get(keys[i]))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static void processChild(List<Node> result, Node childNode, String childnameString) {
        String name =  childNode.getNodeName();
        String[] names = getChildNamesFromString(childnameString,name);
        HashMap <String,String> attributes = getAttributesFromChildName(childnameString);
        for (String n:names) {
            if (name.equals(n) && hasAttributes(childNode,attributes)) {
                result.add(childNode);
            }
        }
    }
    
    private static List<Node> getChildListFromRootList(List<Node> rootlist, String childname) {
        List<Node> result = new LinkedList<Node>();
        
        for (int i = 0; i < rootlist.size(); i++) {
            Node node = rootlist.get(i);
            if (node == null) {
                continue;
            }
            
            NodeList childsList = node.getChildNodes();
            for (int j = 0; j < childsList.getLength(); j++) {
                processChild(result, childsList.item(j), childname);
            }
        }
        return result;
    }
}
