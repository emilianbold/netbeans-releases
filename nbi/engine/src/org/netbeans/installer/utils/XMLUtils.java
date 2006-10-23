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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class XMLUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static XMLUtils instance;
    
    public static synchronized XMLUtils getInstance() {
        if (instance == null) {
            instance = new GenericXMLUtils();
        }
        
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract void saveXMLDocument(Document doc, File file,File xsltTransformFile) throws TransformerConfigurationException, TransformerException, IOException;
    
    public abstract List <Node> getChildList(Node root, String ... childs);
    
    public abstract Node getChildNode(Node root, String ... childs) throws ParseException;
    
    public abstract String getChildNodeTextContent(Node root, String ... childs) throws ParseException;
    
    public abstract String getNodeAttribute(Node node, String attrName);
    
    public abstract String getNodeTextContent(Node node);
    
    public abstract Node addChildNode(Node parentNode, String tag, String textContent);
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class GenericXMLUtils extends XMLUtils {
        public void saveXMLDocument(Document doc, File file,File xsltTransformFile) throws TransformerConfigurationException, TransformerException, IOException {
            FileOutputStream outputStream = null;
            try {
                Source xsltSource = new StreamSource(xsltTransformFile);
                Source domSource = new DOMSource(doc);
                outputStream = new FileOutputStream(file);
                Result streamResult = new StreamResult(outputStream);
                
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer(xsltSource);
                
                transformer.transform(domSource, streamResult);
            } finally {
                if(outputStream!=null) {
                    outputStream.close();
                }
            }
        }
        // get an array of String from string "(Name1,Name2,Name3)"
        private String [] getChildNamesFromString(String childname, String name) {
            String [] result;
            if(childname!=null && childname.equals(name)) {
                result = new String [] { childname };
            } else if(childname.startsWith("(") && childname.endsWith(")")) {
                // several childs in round brackets separated by commas
                int len = childname.length();
                String [] names = childname.substring(1,len-1).split(",");
                int index =0;
                for(String n:names) {
                    if(n!=null && name.equals(n)) {
                        index ++;
                    }
                }
                result = new String [index];
                index  = 0 ;
                for(String n:names) {
                    if(n!=null && name.equals(n)) {
                        result[index] = n;
                        index ++;
                    }
                }
            }  else {
                result = new String [] {};
            }
            return result;
        }
        private HashMap <String,String> getAttributesFromChildName(String childname) {
            HashMap <String,String> map = new HashMap <String,String> ();
            
            int start = childname.indexOf(ATTR_BEGIN);
            int end = childname.indexOf(ATTR_END);
            if(start!=-1 && end == (childname.length()-1 )) {
                // child with specified attribute
                String sub = childname.substring(start + ATTR_BEGIN.length(), end);
                String [] attrs = sub.split(ATTRS_DELIM);
                for(String s: attrs) {
                    String [] nameValue = s.split(ATTRS_DELIM);
                    if(nameValue.length==2) {
                        if(nameValue[1].indexOf("\"")==0 && nameValue[1].lastIndexOf("\"")==(nameValue[1].length()-1)) {
                            nameValue[1] = nameValue[1].substring(1,nameValue[1].length()-1);
                        }
                        map.put(nameValue[0],nameValue[1]);
                    }
                }
                
            }
            return map;
        }
        private boolean hasAttributes(Node childNode, HashMap <String, String> attributes) {
            int size = attributes.size();
            if(size==0) {
                return true;
            } else {
                Object [] keys = attributes.keySet().toArray();
                for(int i=0;i<size;i++) {
                    if(keys[i] instanceof String) {
                        if(!getNodeAttribute(childNode,(String)keys[i]).equals(attributes.get(keys[i]))) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        private void processChild(List <Node> result, Node childNode, String childnameString) {
            String name =  childNode.getNodeName();
            String [] names = getChildNamesFromString(childnameString,name);
            HashMap <String,String> attributes = getAttributesFromChildName(childnameString);
            for(String n:names) {
                if(n!=null && name.equals(n) && hasAttributes(childNode,attributes)) {
                    result.add(childNode);
                }
            }
        }
        private List <Node> getChildListFromRootList(List <Node> rootlist, String childname) {
            List <Node> result = new LinkedList <Node> ();
            if(rootlist == null) {
                return result;
            }
            for(int i=0;i<rootlist.size();i++) {
                Node node = rootlist.get(i);
                if(node==null) {
                    continue;
                }
                NodeList childsList = node.getChildNodes();
                for(int j=0;j<childsList.getLength();j++) {
                    processChild(result, childsList.item(j),childname);
                }
            }
            return result;
        }
        
        public List <Node> getChildList(Node root, String ... childs) {
            
            List <Node> resultList = null;
            NodeList nodeList = null;
            String name;
            if(root != null)  {
                resultList = new LinkedList <Node> ();
                resultList.add(root);
                if(childs!=null) {
                    if(childs.length==1) {
                        if(childs[0].startsWith("./")) {
                            childs [0] = childs [0].substring("./".length());
                        }
                        childs = childs [0].split("/");
                    }
                    
                    for(String child: childs) {
                        resultList = getChildListFromRootList(resultList,child);
                    }
                }
            }
            return resultList;
        }
        
        public Node getChildNode(Node root, String ... childs) throws ParseException {
            List <Node> list = getChildList(root,childs);
            if(list == null) {
                return null;
            }
            if(list.size()==0) {
                return null;
            }
            if(list.size()>1 ) {
                throw new ParseException("Requested single node, returned " +
                        list.size() + " nodes");
            }
            return list.get(0);
        }
        public String getNodeAttribute(Node node, String attrName) {
            String str = null;
            
            if(node!=null && attrName!=null) {
                NamedNodeMap map = node.getAttributes();
                if(map!=null) {
                    if(attrName.startsWith("./@")) {
                        attrName = attrName.substring("./@".length());
                    }
                    Node attr = map.getNamedItem(attrName);
                    if(attr!=null && attr.getNodeType() == Node.ATTRIBUTE_NODE) {
                        str = attr.getNodeValue();
                    }
                }
            }
            
            return  str;
        }
        
        public String getNodeTextContent(Node node) {
            return (node==null) ? null : node.getTextContent();
            
        }
        public String getChildNodeTextContent(Node root, String ... childs) throws ParseException {
            return getNodeTextContent(getChildNode(root,childs));
        }
        
        public Node addChildNode(Node parentNode, String tag, String textContent) {
            Node result = null;
            if(parentNode!=null && tag!=null) {
                
                result = parentNode.getOwnerDocument().createElement(tag);
                if(textContent!=null) {
                    result.setTextContent(textContent);
                }
                parentNode.appendChild(result);
            }
            return result;
        }
        private static final String ATTR_BEGIN = "[@";
        private static final String ATTR_END = "]";
        private static final String ATTR_DELIM = "=";
        private static final String ATTRS_DELIM = " and ";
    }
}
