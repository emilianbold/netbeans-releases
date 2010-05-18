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

package org.netbeans.test.xml.schema.lib.dom.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.netbeans.test.xml.schema.lib.types.ComponentCategories;


/**
 *
 * @author ca@netbeans.org
 */
public class SchemaDOMBuilder {
    
    public static final int SCHEMA_LEVEL            = 1;
    public static final int GLOBAL_COMPONENTS_LEVEL = 2;
    
    Node m_schema = null;
    TreeMap<String, Node> m_globalAttributes        = new TreeMap<String, Node>();
    TreeMap<String, Node> m_globalAttributeGroups   = new TreeMap<String, Node>();
    TreeMap<String, Node> m_globalComplexTypes      = new TreeMap<String, Node>();
    TreeMap<String, Node> m_globalElements          = new TreeMap<String, Node>();
    TreeMap<String, Node> m_globalGroups            = new TreeMap<String, Node>();
    TreeMap<String, Node> m_referencedSchemas       = new TreeMap<String, Node>();
    TreeMap<String, Node> m_globalSimpleTypes       = new TreeMap<String, Node>();
    
    public int m_lineNmb;
    private String m_strFileName;
    
    private Document m_doc = null;
    
    /**
     * Creates a new instance of SchemaDOMBuilder
     */
    public SchemaDOMBuilder() {
    }
    
    /**
     * @param args the command line arguments
     */
    
    public void setFileName(String fileName) {
        m_strFileName = fileName;
    }
    
    public void setInitialLineNumber(int lineNmb) {
        m_lineNmb = lineNmb;
    }
    
    public NodeIterator getNodeIterator(ComponentCategories category) {
        NodeIterator iterator = null;
        
        switch (category) {
            case ATTRIBUTES:
                iterator = new NodeIterator(m_globalAttributes);
                break;
            case ATTRIBUTE_GROUPS:
                iterator = new NodeIterator(m_globalAttributeGroups);
                break;
            case COMPLEX_TYPES:
                iterator = new NodeIterator(m_globalComplexTypes);
                break;
            case ELEMENTS:
                iterator = new NodeIterator(m_globalElements);
                break;
            case GROUPS:
                iterator = new NodeIterator(m_globalGroups);
                break;
            case REFERENCED_SCHEMAS:
                iterator = new NodeIterator(m_referencedSchemas);
                break;
            case SIMPLE_TYPES:
                iterator = new NodeIterator(m_globalSimpleTypes);
                break;
        }
        return iterator;
    }
    
    public void build() {
        File file = new File(m_strFileName);
        
        try {
            m_doc = getDocumentBuilder().parse(file);
        } catch (SAXException e) {
            System.out.println("SAXException: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            return;
        }
        
        categorizeNodes(m_doc, 0);
        
        adaptDocToColumnView(m_doc);
    }
    
    
    private void adaptDocToColumnView(Node node) {
        
        if (node.getNodeType() ==  Node.ELEMENT_NODE) {
            String strComponentName = removePrefix(node.getNodeName());
            
            if (strComponentName.equals("simpleType")) {
                NodeList childList = node.getChildNodes();
                
                for (int i = 0; i < childList.getLength(); i++) {
                    Node child = childList.item(i);
                    String childName = removePrefix(child.getNodeName());
                    if (childName.equals("restriction")) {
                        NodeList childList1 = child.getChildNodes();
                        for (int j = 0; j < childList1.getLength(); j++) {
                            Node child1 = childList1.item(j);
                            String childName1 = removePrefix(child1.getNodeName());
                            if (childName1.equals("enumeration")) {
                                node.appendChild(child1);
                                j--;
                            } else {
                                child.removeChild(child1);
                                j--;
                            }
                        }
                        node.removeChild(child);
                        i--;
                    } else if (childName.equals("list")) {
                        node.removeChild(child);
                        i--;
                    }
                }
            } else if (strComponentName.equals("complexType")) {
                NodeList childList = node.getChildNodes();
                
                for (int i = 0; i < childList.getLength(); i++) {
                    Node child = childList.item(i);
                    String childName = removePrefix(child.getNodeName());
                    if (childName.equals("simpleContent") || childName.equals("complexContent")) {
                        NodeList childList1 = child.getChildNodes();
                        for (int j = 0; j < childList1.getLength(); j++) {
                            Node child1 = childList1.item(j);
                            String childName1 = removePrefix(child1.getNodeName());
                            if (childName1.equals("restriction") || childName1.equals("extension")) {
                                NodeList childList2 = child1.getChildNodes();
                                for (int k = 0; k < childList2.getLength(); k++) {
                                    Node child2 = childList2.item(k);
                                    node.appendChild(child2);
                                    k--;
                                }
                                child.removeChild(child1);
                                j--;
                            }
                            node.removeChild(child);
                            i--;
                        }
                    }
                }
            }
        }
        
        NodeList childList = node.getChildNodes();
        
        for (int i = 0; i < childList.getLength(); i++) {
            adaptDocToColumnView(childList.item(i));
        }
        
    }
    
    public static void main(String[] args) {
        
        SchemaDOMBuilder builder = new SchemaDOMBuilder();
        
        builder.setFileName(args[0]);
        
        builder.build();
        
        builder.printNodes(builder.m_doc, 0);
    }
    
    private void printGlobalComponents(TreeMap<String, Node> map) {
        Collection c = map.values();
        Iterator iterator = c.iterator();
        while(iterator.hasNext()) {
            Node node = (Node) iterator.next();
            ExtraNodeInfo sn = ExtraNodeInfo.getExtraNodeInfo(node);
            printNodeInfo(node);
            System.out.println("\n line " + sn.getLineNmb());
            System.out.println(" component name " + sn.getComponentName());
        }
    }
    
    private static DocumentBuilder getDocumentBuilder() {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        DocumentBuilder builder = null;
        
        try {
            builder =  factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {}
        
        return builder;
    }
    
    private void categorizeNodes(Node node, int level) {
        
        while (true) {
            String strValue = node.getNodeValue();
            if (strValue != null) {
                char[] value = strValue.toCharArray();
                
                for (int i = 0; i < value.length; i++) {
                    if (value[i] == '\n') {
                        m_lineNmb++;
                    }
                }
            }
            
            switch (node.getNodeType()) {
                case Node.TEXT_NODE:
                    strValue = strValue.replace("\r\n", "").trim();
                    if (strValue.length() == 0) {
                        Node parent = node.getParentNode();
                        Node nextNode = node.getNextSibling();
                        parent.removeChild(node);
                        node = nextNode;
                        if (node != null) continue;
                        return;
                    }
                case Node.COMMENT_NODE:
                    Node parent1 = node.getParentNode();
                    Node nextNode1 = node.getNextSibling();
                    parent1.removeChild(node);
                    node = nextNode1;
                    if (node != null) continue;
                    return;
            }
            break;
        }
        
        String strComponentName = removePrefix(node.getNodeName());
        ExtraNodeInfo schemaNode = new ExtraNodeInfo(m_lineNmb, strComponentName, node, level == GLOBAL_COMPONENTS_LEVEL);
        node.setUserData("", schemaNode, null);
        
        switch (level) {
            case GLOBAL_COMPONENTS_LEVEL:
                NamedNodeMap map = node.getAttributes();
                if (map != null) {
                    Node nameAttrNode = map.getNamedItem("name");
                    if (strComponentName.equals("attribute")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalAttributes.put(strName, node);
                    } else if (strComponentName.equals("attributeGroup")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalAttributeGroups.put(strName, node);
                    } else if (strComponentName.equals("complexType")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalComplexTypes.put(strName, node);
                    } else if (strComponentName.equals("element")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalElements.put(strName, node);
                    } else if (strComponentName.equals("group")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalGroups.put(strName, node);
                    } else if (strComponentName.equals("include") || strComponentName.equals("import") || strComponentName.equals("redefine")) {
                    } else if (strComponentName.equals("simpleType")) {
                        String strName = nameAttrNode.getNodeValue();
                        m_globalSimpleTypes.put(strName, node);
                    }
                }
                break;
            case SCHEMA_LEVEL:
                m_schema = node;
                break;
        }
        
        NodeList childList = node.getChildNodes();
        
        for (int i = 0; i < childList.getLength(); i++) {
            categorizeNodes(childList.item(i), level+1);
        }
    }
    
    public void print( )
    {
      printNodes( m_doc, 0 );
    }

    private void printNodes(Node node, int level) {
        
        System.out.println();
        System.out.print("*");
        
        for (int j = 0; j < level; j++) {
            System.out.print("|");
        }
        
        printNodeInfo(node);
        
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            printNodes(child, level+1);
        }
    }
    
    private void printNodeInfo(Node node) {
        System.out.print("Component [" + node.getNodeName() + "], Node type [" + node.getNodeType() + "], Node value [" + node.getNodeValue() + "]");
        NamedNodeMap attrMap = node.getAttributes();
        
        if (attrMap != null && attrMap.getLength() > 0) {
            System.out.print(", Attrs: ");
            for (int i = 0; i < attrMap.getLength(); i++) {
                System.out.print(attrMap.item(i).getNodeName() + "=\"" + attrMap.item(i).getNodeValue() + "\" ");
            }
        }
    }
    
    private String removePrefix(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(":")+1);
    }
}
